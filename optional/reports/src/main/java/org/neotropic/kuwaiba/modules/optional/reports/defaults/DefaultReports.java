/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.modules.optional.reports.defaults;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectList;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.RelTypes;


/**
 * Temporary class that provides methods to build class reports
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class DefaultReports {
    
    private MetadataEntityManager mem;
    private ApplicationEntityManager aem;
    private BusinessEntityManager bem;
    public String corporateLogo;
    
    public static final String RELATIONSHIP_IPAMHASADDRESS = "ipamHasIpAddress";
    public static final String RELATIONSHIP_IPAMPORTRELATEDTOINTERFACE = "ipamportrelatedtointerface";
    public static final String RELATIONSHIP_IPAMBELONGSTOVLAN = "ipamBelongsToVlan";
    
    public DefaultReports(MetadataEntityManager mem, BusinessEntityManager bem, ApplicationEntityManager aem) {
        this.mem = mem;
        this.aem = aem;
        this.bem = bem;
        this.corporateLogo = aem.getConfiguration().getProperty("corporateLogo") == null ? "logo.jpg" : aem.getConfiguration().getProperty("corporateLogo");
    }
  
//    public RawReport buildTransportLinkUsageReport (String transportLinkClass, String transportLinkId) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException, NoCommercialModuleFoundException {
//        String query = String.format("MATCH (transportLink)-[relation:%s]-(port)-[:%s*]->(equipment)-[:%s]->(class)-[:%s*]->(superClass) "
//                    + "WHERE transportLink._uuid = \"%s\" AND superClass.name = \"%s\" AND (relation.name = \"%s\" OR relation.name = \"%s\")"
//                    + "RETURN transportLink, equipment, port",  RelTypes.RELATED_TO_SPECIAL, RelTypes.CHILD_OF, RelTypes.INSTANCE_OF, 
//                            RelTypes.EXTENDS, transportLinkId, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, 
//                            SDHModule.RELATIONSHIP_SDHTLENDPOINTA, SDHModule.RELATIONSHIP_SDHTLENDPOINTB);
//        HashMap<String, BusinessObjectList> theResult = aem.executeCustomDbCode(query, true);
//        
//        String title, transportLinkUsageReportText;
//        BusinessObject theTransportLink;
//        
//        if (theResult.get("transportLink").getList().isEmpty()) {
//            title = "Error";
//            transportLinkUsageReportText = getHeader(title);
//            transportLinkUsageReportText += "<div class=\"error\">No information about this transport link could be found</div>";
//        }
//        else {
//            theTransportLink = theResult.get("transportLink").getList().get(0);
//            title = "Transport Link Usage Report for " + theTransportLink.getName();
//            transportLinkUsageReportText = getHeader(title);
//            transportLinkUsageReportText += 
//                                "  <body><table><tr><td><h1>" + title + "</h1></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";
//            
//            //General Info
//            transportLinkUsageReportText += "<table><tr><td class=\"generalInfoLabel\">Name</td><td class=\"generalInfoValue\">" + theTransportLink.getName() + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Type</td><td class=\"generalInfoValue\">" + theTransportLink.getClassName() + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Endpoint A</td><td class=\"generalInfoValue\"><b>" + theResult.get("equipment").getList().get(0) + "</b>:" + theResult.get("port").getList().get(0).getName() + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Endpoint B</td><td class=\"generalInfoValue\"><b>" + theResult.get("equipment").getList().get(1) + "</b>:" + theResult.get("port").getList().get(1).getName() + "</td></tr></table>";
//            
//            //Structure
//            String transportLinkStructure;
//            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
//            List<SDHContainerLinkDefinition> sdhTransportLinkStructure = sdhModule.getSDHTransportLinkStructure(transportLinkClass, transportLinkId);
//            int totalLinkCapacity = SDHModule.calculateTransportLinkCapacity(transportLinkClass);
//            if (totalLinkCapacity == 0)
//                transportLinkStructure = "<div class=\"error\">The transport link class does not allow automatic capacity calculation</div>";
//            else {
//                transportLinkStructure = "<table><tr><th>Position</th><th>Container Name</th><th>Structured</th></tr>\n";
//                SDHContainerLinkDefinition[] allContainers = new SDHContainerLinkDefinition[totalLinkCapacity];
//                for (SDHContainerLinkDefinition containerDefinition : sdhTransportLinkStructure) {
//                    int firstPosition = containerDefinition.getPositions().get(0).getPosition();
//                    //Handles concatenated containers
//                    allContainers[firstPosition - 1] = containerDefinition;
//                    int adjacentPositions = SDHModule.calculateContainerLinkCapacity(containerDefinition.getContainer().getClassName()) - 1;
//                    for (int i = firstPosition; i < firstPosition + adjacentPositions; i ++ )
//                        allContainers[i] = containerDefinition;
//                }
//                
//                for (int i = 0; i < allContainers.length; i++) {
//                    if (allContainers[i] == null)
//                        transportLinkStructure += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd") + "\"><td>" + (i + 1) + "</td><td>Free</td><td>NA</td></tr>";
//                    else
//                        transportLinkStructure += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd") + "\"><td>" + (i + 1) + "</td><td>" + allContainers[i].getContainer() + "</td><td>" + (allContainers[i].isStructured() ? "Yes" : "No") + "</td></tr>";
//                }
//                
//                transportLinkStructure += "</table>";
//            }
//            transportLinkUsageReportText += transportLinkStructure;
//        }
//        
//        transportLinkUsageReportText += getFooter();
//        
//        return new RawReport("Transport Link Usage", "Neotropic SAS","1.1", transportLinkUsageReportText);
//    }
//    
//    public RawReport buildLowOrderTributaryLinkDetailReport (String tributaryLinkClass, String tributaryLinkId) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
//        String query = String.format("MATCH (customerSuperClass)<-[:%s*]-(customerClass)<-[:%s]-(customer)<-[:%s*]-(service)-[relationA:%s]->(tributaryLink)-[relationB:%s]-(port)-[:%s*]->(equipment)-[:%s]->(class)-[:%s*]->(superClass) "
//                + "WHERE tributaryLink._uuid = \"%s\" AND superClass.name=\"%s\" AND relationA.name = \"%s\" AND (relationB.name = \"%s\" OR relationB.name = \"%s\") AND customerSuperClass.name=\"%s\" RETURN tributaryLink, customer, service, port, equipment", 
//                    RelTypes.EXTENDS, RelTypes.INSTANCE_OF, RelTypes.CHILD_OF_SPECIAL, RelTypes.RELATED_TO_SPECIAL, RelTypes.RELATED_TO_SPECIAL, 
//                    RelTypes.CHILD_OF, RelTypes.INSTANCE_OF, RelTypes.EXTENDS, tributaryLinkId, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, "uses", 
//                    SDHModule.RELATIONSHIP_SDHTTLENDPOINTA, SDHModule.RELATIONSHIP_SDHTTLENDPOINTB, Constants.CLASS_GENERICCUSTOMER);
//        HashMap<String, BusinessObjectList> theResult = aem.executeCustomDbCode(query, true);
//        
//        String title, tributaryLinkUsageReportText;
//        BusinessObject theTributaryLink;
//        
//        if (theResult.get("tributaryLink").getList().isEmpty()) {
//            title = "Error";
//            tributaryLinkUsageReportText = getHeader(title);
//            tributaryLinkUsageReportText += "<div class=\"error\">No information about this tributary link could be found</div>";
//        }
//        else {
//            theTributaryLink = theResult.get("tributaryLink").getList().get(0);
//            title = "Tributary Link Details Report for " + theTributaryLink.getName();
//            tributaryLinkUsageReportText = getHeader(title);
//            tributaryLinkUsageReportText += 
//                                "  <body><table><tr><td><h1>" + title + "</h1></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";
//            
//            //Demarcation points
//            query = String.format("MATCH (tributaryLink)-[relationA:%s]-(equipmentPort)-[relationB:%s]-(physicalConnection)-[relationC:%s]-(nextEquipmentPort)-[:%s*]->(nextEquipment)-[:%s]->(class)-[:%s*]->(superClass) "
//                + "WHERE tributaryLink._uuid = \"%s\" AND (relationA.name = \"%s\" OR relationA.name = \"%s\") "
//                    + "AND (relationB.name = \"%s\" OR relationB.name = \"%s\") "
//                    + "AND (relationC.name = \"%s\" OR relationC.name = \"%s\") "
//                    + "AND superClass.name=\"%s\" "
//                + "RETURN nextEquipmentPort, nextEquipment", RelTypes.RELATED_TO_SPECIAL, RelTypes.RELATED_TO_SPECIAL, RelTypes.RELATED_TO_SPECIAL, RelTypes.CHILD_OF, RelTypes.INSTANCE_OF, RelTypes.EXTENDS, 
//                                    tributaryLinkId, SDHModule.RELATIONSHIP_SDHTTLENDPOINTA, SDHModule.RELATIONSHIP_SDHTTLENDPOINTB,
//                                    "endpointA", "endpointB", "endpointA", "endpointB", Constants.CLASS_GENERICDISTRIBUTIONFRAME);
//            
//            HashMap<String, BusinessObjectList> demarcationPoints = aem.executeCustomDbCode(query,true);
//            String demarcationPointsAsSring = "";
//            for (int i = 0; i < demarcationPoints.get("nextEquipmentPort").getList().size(); i++)
//                demarcationPointsAsSring += "<b>" + demarcationPoints.get("nextEquipment").getList().get(i) + "</b>:" + demarcationPoints.get("nextEquipmentPort").getList().get(i) + "<br/>";
//            
//            //General Info
//            tributaryLinkUsageReportText += "<table><tr><td class=\"generalInfoLabel\">Name</td><td class=\"generalInfoValue\">" + theTributaryLink.getName() + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Type</td><td class=\"generalInfoValue\">" + theTributaryLink.getClassName() + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Endpoint A</td><td class=\"generalInfoValue\"><b>" + theResult.get("equipment").getList().get(0) + "</b>:" + theResult.get("port").getList().get(0).getName() + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Endpoint B</td><td class=\"generalInfoValue\"><b>" + theResult.get("equipment").getList().get(1) + "</b>:" + theResult.get("port").getList().get(1).getName() + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Demarcation Points</td><td class=\"generalInfoValue\">" + demarcationPointsAsSring + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Service</td><td class=\"generalInfoValue\">" + theResult.get("service").getList().get(0).getName() + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Customer</td><td class=\"generalInfoValue\">" + theResult.get("customer").getList().get(0).getName() + "</td></tr></table>";
//        
//            //Used resources
//            List<BusinessObjectLight> container = bem.getSpecialAttribute(tributaryLinkClass, tributaryLinkId, SDHModule.RELATIONSHIP_SDHDELIVERS);
//            
//            String usedResources;
//            if (container.isEmpty())
//                usedResources = "<div class=\"error\">This tributary link seems malformed and does not have a path</div>";
//            else {
//                List<AnnotatedBusinessObjectLight> structured = bem.getAnnotatedSpecialAttribute(container.get(0).getClassName(), 
//                        container.get(0).getId(), SDHModule.RELATIONSHIP_SDHCONTAINS);
//                usedResources = "<table><tr><th>Structured Name</th><th>Position in Container</th><th>Transport Links</th></tr>";
//                int i = 0;
//                for (AnnotatedBusinessObjectLight aStructured : structured) {
//                    String transportLinksString = "";
//                    
//                    List<AnnotatedBusinessObjectLight> transportLinks = 
//                            bem.getAnnotatedSpecialAttribute(aStructured.getObject().getClassName(), aStructured.getObject().getId(), SDHModule.RELATIONSHIP_SDHTRANSPORTS);
//                    
//                    for (AnnotatedBusinessObjectLight transportLink : transportLinks)
//                        transportLinksString += transportLink.getProperties().get(SDHModule.PROPERTY_SDHPOSITION) + " - " + transportLink.getObject() + "<br/>";
//                    
//                    usedResources += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd") +"\"><td>" + aStructured.getObject() + "</td>"
//                                    + "<td>" + asKLM((int)aStructured.getProperties().get(SDHModule.PROPERTY_SDHPOSITION)) + "</td>"
//                                    + "<td>" + transportLinksString + "</td></tr>";
//                    
//                    i ++;
//                }
//                usedResources += "</table>";
//            }
//            tributaryLinkUsageReportText += usedResources;
//        }
//        tributaryLinkUsageReportText += getFooter();
//        
//        return new RawReport("Tributary Link Details", "Neotropic SAS","1.1", tributaryLinkUsageReportText);
//    }
//
//    public RawReport buildHighOrderTributaryLinkDetailReport (String tributaryLinkClass, String tributaryLinkId) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
//        String query = String.format("MATCH (customerSuperClass)<-[:%s*]-(customerClass)<-[:%s]-(customer)<-[:%s*]-(service)-[relationA:%s]->(tributaryLink)-[relationB:%s]-(port)-[:%s*]->(equipment)-[:%s]->(class)-[:%s*]->(superClass) "
//                + "WHERE tributaryLink._uuid = \"%s\" AND superClass.name=\"%s\" AND relationA.name = \"%s\" AND (relationB.name = \"%s\" OR relationB.name = \"%s\") AND customerSuperClass.name=\"%s\" RETURN tributaryLink, customer, service, port, equipment", 
//                    RelTypes.EXTENDS, RelTypes.INSTANCE_OF, RelTypes.CHILD_OF_SPECIAL, RelTypes.RELATED_TO_SPECIAL, RelTypes.RELATED_TO_SPECIAL, 
//                    RelTypes.CHILD_OF, RelTypes.INSTANCE_OF, RelTypes.EXTENDS, tributaryLinkId, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, "uses", 
//                    SDHModule.RELATIONSHIP_SDHTTLENDPOINTA, SDHModule.RELATIONSHIP_SDHTTLENDPOINTB, Constants.CLASS_GENERICCUSTOMER);
//        HashMap<String, BusinessObjectList> theResult = aem.executeCustomDbCode(query, true);
//        
//        String title, tributaryLinkUsageReportText;
//        BusinessObject theTributaryLink;
//        
//        if (theResult.get("tributaryLink").getList().isEmpty()) {
//            title = "Error";
//            tributaryLinkUsageReportText = getHeader(title);
//            tributaryLinkUsageReportText += "<div class=\"error\">No information about this tributary link could be found</div>";
//        }
//        else {
//            theTributaryLink = theResult.get("tributaryLink").getList().get(0);
//            title = "Tributary Link Details Report for " + theTributaryLink.getName();
//            tributaryLinkUsageReportText = getHeader(title);
//            tributaryLinkUsageReportText += 
//                                "  <body><table><tr><td><h1>" + title + "</h1></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";
//            
//            //Demarcation points
//            query = String.format("MATCH (tributaryLink)-[relationA:%s]-(equipmentPort)-[relationB:%s]-(physicalConnection)-[relationC:%s]-(nextEquipmentPort)-[:%s*]->(nextEquipment)-[:%s]->(class)-[:%s*]->(superClass) "
//                + "WHERE tributaryLink._uuid = \"%s\" AND (relationA.name = \"%s\" OR relationA.name = \"%s\") "
//                    + "AND (relationB.name = \"%s\" OR relationB.name = \"%s\") "
//                    + "AND (relationC.name = \"%s\" OR relationC.name = \"%s\") "
//                    + "AND superClass.name=\"%s\" "
//                + "RETURN nextEquipmentPort, nextEquipment", RelTypes.RELATED_TO_SPECIAL, RelTypes.RELATED_TO_SPECIAL, RelTypes.RELATED_TO_SPECIAL, RelTypes.CHILD_OF, RelTypes.INSTANCE_OF, RelTypes.EXTENDS, 
//                                    tributaryLinkId, SDHModule.RELATIONSHIP_SDHTTLENDPOINTA, SDHModule.RELATIONSHIP_SDHTTLENDPOINTB,
//                                    "endpointA", "endpointB", "endpointA", "endpointB", Constants.CLASS_GENERICDISTRIBUTIONFRAME);
//            
//            HashMap<String, BusinessObjectList> demarcationPoints = aem.executeCustomDbCode(query, true);
//            String demarcationPointsAsSring = "";
//            for (int i = 0; i < demarcationPoints.get("nextEquipmentPort").getList().size(); i++)
//                demarcationPointsAsSring += "<b>" + demarcationPoints.get("nextEquipment").getList().get(i) + "</b>:" + demarcationPoints.get("nextEquipmentPort").getList().get(i) + "<br/>";
//
//            //General Info
//            tributaryLinkUsageReportText += "<table><tr><td class=\"generalInfoLabel\">Name</td><td class=\"generalInfoValue\">" + theTributaryLink.getName() + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Type</td><td class=\"generalInfoValue\">" + theTributaryLink.getClassName() + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Endpoint A</td><td class=\"generalInfoValue\"><b>" + theResult.get("equipment").getList().get(0) + "</b>:" + theResult.get("port").getList().get(0).getName() + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Endpoint B</td><td class=\"generalInfoValue\"><b>" + theResult.get("equipment").getList().get(1) + "</b>:" + theResult.get("port").getList().get(1).getName() + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Demarcation Points</td><td class=\"generalInfoValue\">" + demarcationPointsAsSring + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Service</td><td class=\"generalInfoValue\">" + theResult.get("service").getList().get(0).getName() + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Customer</td><td class=\"generalInfoValue\">" + theResult.get("customer").getList().get(0).getName() + "</td></tr></table>";
//        
//            //Used resources
//            List<BusinessObjectLight> container = bem.getSpecialAttribute(tributaryLinkClass, tributaryLinkId, SDHModule.RELATIONSHIP_SDHDELIVERS);
//            
//            String usedResources;
//            if (container.isEmpty())
//                usedResources = "<div class=\"error\">This tributary link seems malformed and does not have a path</div>";
//            else {
//                List<AnnotatedBusinessObjectLight> transportLinks = bem.getAnnotatedSpecialAttribute(container.get(0).getClassName(), 
//                        container.get(0).getId(), SDHModule.RELATIONSHIP_SDHTRANSPORTS);
//                usedResources = "<table><tr><th>Transport Link Name</th><th>Transport Link Position</th></tr>";
//               
//                int i = 0;
//                for (AnnotatedBusinessObjectLight transportLink : transportLinks) {
//                    usedResources += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd") +"\"><td>" + transportLink.getObject() + "</td>"
//                                    + "<td>" + transportLink.getProperties().get(SDHModule.PROPERTY_SDHPOSITION) +"</td></tr>";
//                    i ++;
//                }
//                usedResources += "</table>";
//            }
//            tributaryLinkUsageReportText += usedResources;
//        }
//        tributaryLinkUsageReportText += getFooter();
//        
//        return new RawReport("Tributary Link Details", "Neotropic SAS","1.1", tributaryLinkUsageReportText);
//    }
//    
//    public RawReport buildHighOrderTributaryLinkDetailReport2 (String tributaryLinkClass, String tributaryLinkId) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
//        String query = String.format("MATCH (customerSuperClass)<-[:%s*]-(customerClass)<-[:%s]-(customer)<-[:%s*]-(service)-[relationA:%s]->(tributaryLink)-[relationB:%s]-(port)-[:%s*]->(equipment)-[:%s]->(class)-[:%s*]->(superClass) "
//                + "WHERE tributaryLink._uuid = \"%s\" AND superClass.name=\"%s\" AND relationA.name = \"%s\" AND (relationB.name = \"%s\" OR relationB.name = \"%s\") AND customerSuperClass.name=\"%s\" RETURN tributaryLink, customer, service, port, equipment", 
//                    RelTypes.EXTENDS, RelTypes.INSTANCE_OF, RelTypes.CHILD_OF_SPECIAL, RelTypes.RELATED_TO_SPECIAL, RelTypes.RELATED_TO_SPECIAL, 
//                    RelTypes.CHILD_OF, RelTypes.INSTANCE_OF, RelTypes.EXTENDS, tributaryLinkId, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, "uses", 
//                    SDHModule.RELATIONSHIP_SDHTTLENDPOINTA, SDHModule.RELATIONSHIP_SDHTTLENDPOINTB, Constants.CLASS_GENERICCUSTOMER);
//        HashMap<String, BusinessObjectList> theResult = aem.executeCustomDbCode(query, true);
//        
//        String title, tributaryLinkUsageReportText;
//        BusinessObject theTributaryLink;
//        
//        if (theResult.get("tributaryLink").getList().isEmpty()) {
//            title = "Error";
//            tributaryLinkUsageReportText = "<!DOCTYPE html>\n" +
//                    "<html lang=\"en\">\n" +
//                    "  <head>\n" +
//                    "    <meta charset=\"utf-8\">\n" +
//                    "    <title>" + title + "</title>\n</head>\n";
//            tributaryLinkUsageReportText += "<div class=\"error\">No information about this tributary link could be found</div>";
//        } else {
//            String serverName, serverPort;
//            try {
//            serverName = (String)aem.getConfigurationVariableValue("general.misc.serverName");
//            } catch (ApplicationObjectNotFoundException ex) {
//                serverName = "127.0.0.1";
//            }
//
//            try {
//                serverPort = String.valueOf(aem.getConfigurationVariableValue("general.misc.serverPort"));
//            } catch (ApplicationObjectNotFoundException ex) {
//                serverPort = "8181";
//            }    
//                
////            try {
////                logoURL = (String)aem.getConfigurationVariableValue("general.misc.logoURL");
////            } catch (ApplicationObjectNotFoundException ex) {
////                logoURL = "http://neotropic.co/img/logo_blue.png";
////            }
//            
//            
//            theTributaryLink = theResult.get("tributaryLink").getList().get(0);
//            title = "Tributary Link Details Report for " + theTributaryLink.getName();
//            tributaryLinkUsageReportText = "<!DOCTYPE html>\n" +
//                    "<html lang=\"en\">\n" +
//                    "  <head>\n" +
//                    "    <meta charset=\"utf-8\">\n" +
//                    "    <title>" + title + "</title>\n</head>\n";
//            tributaryLinkUsageReportText += 
//                                "  <body>\n";
//            
//            tributaryLinkUsageReportText += "<link rel=\"stylesheet\" href=\"https://" + serverName + ":" + serverPort + "/css/report_01.css\" type=\"text/css\">";
//            tributaryLinkUsageReportText += "<script type=\"text/javascript\" src=\"https://" + serverName + ":" + serverPort + "/js/jsplumb.min.js\"></script>";
//            
//            //Demarcation points
//            query = String.format("MATCH (tributaryLink)-[relationA:%s]-(equipmentPort)-[relationB:%s]-(physicalConnection)-[relationC:%s]-(nextEquipmentPort)-[:%s*]->(nextEquipment)-[:%s]->(class)-[:%s*]->(superClass) "
//                + "WHERE tributaryLink._uuid = \"%s\" AND (relationA.name = \"%s\" OR relationA.name = \"%s\") "
//                    + "AND (relationB.name = \"%s\" OR relationB.name = \"%s\") "
//                    + "AND (relationC.name = \"%s\" OR relationC.name = \"%s\") "
//                    + "AND superClass.name=\"%s\" "
//                + "RETURN nextEquipmentPort, nextEquipment", RelTypes.RELATED_TO_SPECIAL, RelTypes.RELATED_TO_SPECIAL, RelTypes.RELATED_TO_SPECIAL, RelTypes.CHILD_OF, RelTypes.INSTANCE_OF, RelTypes.EXTENDS, 
//                                    tributaryLinkId, SDHModule.RELATIONSHIP_SDHTTLENDPOINTA, SDHModule.RELATIONSHIP_SDHTTLENDPOINTB,
//                                    "endpointA", "endpointB", "endpointA", "endpointB", Constants.CLASS_GENERICDISTRIBUTIONFRAME);
//            
//            HashMap<String, BusinessObjectList> demarcationPoints = aem.executeCustomDbCode(query, true);
//            String demarcationPointsAsSring = "";
//            for (int i = 0; i < demarcationPoints.get("nextEquipmentPort").getList().size(); i++)
//                demarcationPointsAsSring += demarcationPoints.get("nextEquipment").getList().get(i) + ":" + demarcationPoints.get("nextEquipmentPort").getList().get(i) + "<br/>";
//            
//            //The connections diagram
//            tributaryLinkUsageReportText += "<div class=\"container\">\n" +
//"        <div class=\"crossconnection\">\n" +
//"            <div id=\"6bb250d5-3ec5-4367-aa5e-92023a9ba054\" class=\"connectable\"><div class=\"label\">TERACO    <span class=\"note\">South Africa</span></div></div>\n" +
//"            <div id=\"17c9bfd1-ae94-4bff-abfe-88366b1fa578\" class=\"connectable important\"><div class=\"label\"><b>Cape Town</b>    <span class=\"note\">South Africa</span></div></div>\n" +
//"        </div>\n" +
//"        <div class=\"segment segment1\">\n" +
//"            <div id=\"d92c10d3-bf1f-41af-bfa1-989f3b5880f7\" class=\"connectable\"><div class=\"label\">Swakopmund     <span class=\"note\">Namibia</span></div></div>\n" +
//"            <div id=\"6861314c-97fe-48c5-bc3b-920e24967920\" class=\"connectable\"><div class=\"label\">Luanda    <span class=\"note\">Angola</span></div></div>\n" +
//"            <div id=\"47a80055-20f0-42ea-8eb4-d0fe61008ae4\" class=\"connectable\"><div class=\"label\">Muanda    <span class=\"note\">Congo DR</span></div></div>\n" +
//"            <div class=\"segment-text\">Segment 4</div> \n" +
//"        </div>\n" +
//"        <div class=\"crossconnection\">\n" +
//"            <div id=\"b3b04665-a3a6-4e2a-a81f-d93d7497d9e8\" class=\"connectable important\"><div class=\"label\"><b>São Tomé</b>    <span class=\"note\">Sao Tome and Principe</span></div></div>\n" +
//"        </div>\n" +
//"        <div class=\"segment segment2\">\n" +
//"            <div id=\"14887543-7a5d-4825-bc15-d611b26a05aa\" class=\"connectable\"><div class=\"label\">Libreville    <span class=\"note\">Gabon</span></div></div>\n" +
//"            <div id=\"5ef4c2c3-e335-4794-964f-9c90f501ff8b\" class=\"connectable\"><div class=\"label\">Bata    <span class=\"note\">Equatorial Guinea</span></div></div>\n" +
//"            <div id=\"ce2963e4-c797-4a75-a240-29a143a906be\" class=\"connectable\"><div class=\"label\">Lagos    <span class=\"note\">Nigeria</span></div></div>\n" +
//"            <div id=\"61efeab7-4b11-4f43-87a3-97709bcd6ff5\" class=\"connectable\"><div class=\"label\">Porto Novo    <span class=\"note\">Benin</span></div></div>\n" +
//"            <div id=\"061a2727-2bd4-4cd9-8d4a-eb8fdf35b8b6\" class=\"connectable\"><div class=\"label\">Accra    <span class=\"note\">Ghana</span></div></div>\n" +
//"            <div class=\"segment-text\">Segment 3</div> \n" +
//"        </div>\n" +
//"        <div class=\"crossconnection\">\n" +
//"            <div id=\"41033f44-f0e1-4566-8353-5353c7a6f264\" class=\"connectable important\"><div class=\"label\"><b>Abidjan</b>    <span class=\"note\">Côte D'Ivoire</span></div></div>\n" +
//"        </div>\n" +
//"        <div class=\"segment segment3\">\n" +
//"            <div id=\"4c9e49dd-c9ce-4908-aeda-1ab5cd2a1207\" class=\"connectable\"><div class=\"label\">Monrovia    <span class=\"note\">Liberia</span></div></div>\n" +
//"            <div id=\"a419df25-869c-46e2-b442-4ac567e52602\" class=\"connectable\"><div class=\"label\">Freetown    <span class=\"note\">Sierra Leone</span></div></div>\n" +
//"            <div id=\"b76c88e7-1ee5-435b-b3d9-4f6bd2daa539\" class=\"connectable\"><div class=\"label\">Conakry    <span class=\"note\">Guinea</span></div></div>\n" +
//"            <div class=\"padding\"><div id=\"5e952532-b1a5-4dde-9219-a089e62b1aa7\" class=\"connectable\"><div class=\"label\">Bissau    <span class=\"note\">Guinea Bissau</span></div></div></div>\n" +
//"            <div id=\"c85bbb56-0cb6-4b15-89d8-990bc4e905d7\" class=\"connectable\"><div class=\"label\">Banjul    <span class=\"note\">Gambia</span></div></div>\n" +
//"            <div class=\"segment-text\">Segment 2</div> \n" +
//"        </div>\n" +
//"        <div class=\"crossconnection\">\n" +
//"            <div id=\"6f78855a-98b7-4494-82ce-ce2eaafbf2c1\" class=\"connectable important\"><div class=\"label\"><b>Dakar</b>    <span class=\"note\">Senegal</span></div></div>\n" +
//"        </div>\n" +
//"        <div class=\"segment segment4\">\n" +
//"            <div id=\"ed45be7d-fe4a-41ea-aa51-041cde23e583\" class=\"connectable\"><div class=\"label\">Nouakchott    <span class=\"note\">Mauritania</span></div></div>\n" +
//"            <div id=\"ef0b7cc4-2c09-449c-974e-81c4fc383cb4\" class=\"connectable\"><div class=\"label\">Tenerife    <span class=\"note\">Spain</span></div></div>\n" +
//"            <div id=\"1522ff49-d068-4a6f-9eb2-7d3ef8582d94\" class=\"connectable\"><div class=\"label\">Carcavellos    <span class=\"note\">Portugal</span></div></div>\n" +
//"            <div class=\"padding\"><div id=\"9b00b7c5-0fa4-46db-8fc1-1e04082d4f94\" class=\"connectable\"><div class=\"label\">ITConic/Telvent    <span class=\"note\">Portugal</span></div></div></div>\n" +
//"            <div id=\"8eccdf31-2be2-47ee-af95-25d3e421b042\" class=\"connectable important\"><div class=\"label\"><b>Penmarch</b>    <span class=\"note\">France</span></div></div>\n" +
//"            <div id=\"eecfdefb-e224-4552-8f05-a12d6f0ed9c2\" class=\"connectable important\"><div class=\"label\">Paris TH2    <span class=\"note\">France</span></div></div>\n" +
//"            <div class=\"segment-text\" style=\"left:0\">Segment 1</div> \n" +
//"        </div>\n" +
//"    </div>\n";
//
//            //Title
//            tributaryLinkUsageReportText += "<h1>" + title + "</h1>\n";
//            
//            //General Info
//            tributaryLinkUsageReportText += "<table><tr><th colspan=\"2\">General Information</th></tr><tr><td class=\"generalInfoLabel\">Name</td><td class=\"generalInfoLabel\">Service</td></tr>"
//                    + "<tr><td class=\"generalInfoValue\">" + theTributaryLink.getName() + "</td><td class=\"generalInfoValue\">" + theResult.get("service").getList().get(0).getName() + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Endpoint A</td><td class=\"generalInfoLabel\">Endpoint B</td></tr>"
//                    + "<tr><td class=\"generalInfoValue\">" + theResult.get("equipment").getList().get(0) + ":" + theResult.get("port").getList().get(0).getName() + "</td><td class=\"generalInfoValue\">" + theResult.get("equipment").getList().get(1) + ":" + theResult.get("port").getList().get(1).getName() + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">CARF</td><td class=\"generalInfoLabel\">Legal Owner</td></tr>"
//                    + "<tr><td class=\"generalInfoValue\">" + (theTributaryLink.getAttributes().get("hopCarf") == null ? "Not Set" : theTributaryLink.getAttributes().get("hopCarf")) + "</td><td class=\"generalInfoValue\">" + (theTributaryLink.getAttributes().get("hop1LegalOwner") == null ? "Not Set" : bem.getAttributeValueAsString(tributaryLinkClass, tributaryLinkId, "hop1LegalOwner")) + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">MMR</td><td class=\"generalInfoLabel\">Provider ID</td></tr>"
//                    + "<tr><td class=\"generalInfoValue\">" + (theTributaryLink.getAttributes(). get("MMR") == null ? "Not Set" : theTributaryLink.getAttributes().get("MMR")) + "</td><td class=\"generalInfoValue\">" + (theTributaryLink.getAttributes().get("hop1Id") == null ? "Not Set" : theTributaryLink.getAttributes().get("hop1Id")) + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Customer</td><td class=\"generalInfoLabel\">Demarcation Points</td></tr>"
//                    + "<tr><td class=\"generalInfoValue\">" + theResult.get("customer").getList().get(0).getName() + "</td><td class=\"generalInfoValue\">" + demarcationPointsAsSring + "</td></tr></table>";
//            
//            //Used resources
//            List<BusinessObjectLight> container = bem.getSpecialAttribute(tributaryLinkClass, tributaryLinkId, SDHModule.RELATIONSHIP_SDHDELIVERS);
//            
//            String usedResources;
//            if (container.isEmpty())
//                usedResources = "<div class=\"error\">This tributary link seems malformed and does not have a path</div>";
//            else {
//                List<AnnotatedBusinessObjectLight> transportLinks = bem.getAnnotatedSpecialAttribute(container.get(0).getClassName(), 
//                        container.get(0).getId(), SDHModule.RELATIONSHIP_SDHTRANSPORTS);
//                usedResources = "<table><tr><th>Transport Link List</th></tr>";
//               
//                String transportLinksToBeHighlighted = "";
//                for (AnnotatedBusinessObjectLight transportLink : transportLinks) {
//                    usedResources += "<tr><td class=\"generalInfoValue\">" + transportLink.getObject() + "</td></tr>";
//                    transportLinksToBeHighlighted += ("'" + transportLink.getObject().getId() + "',");
//                }
//                usedResources += "</table>";
//                usedResources += "<script>\n"
//                +    "jsPlumb.ready(function() {\n" +            
//"            var expressConnections = {\n" +
//"                connector: [\"Flowchart\", { cornerRadius:40 }],\n" +
//"                anchors:[\"Left\", \"Left\"],\n" +
//"                endpoint:[ \"Dot\", { radius:5 } ]\n" +
//"            };\n" +
//"            var extraConnections = {\n" +
//"                connector: [\"Flowchart\", { stub: [10, 10], cornerRadius: 5, alwaysRespectStubs: true }],\n" +
//"                anchors:[\"Left\", \"Left\"],\n" +
//"                endpoint:[ \"Dot\", { radius:5 } ]\n" +
//"            };\n" +    
//"\n" +
//"            var nonExpressConnections = {\n" +
//"                connector: [\"Straight\"],\n" +
//"                anchors:[\"Left\", \"Left\"],\n" +
//"                endpoint:[ \"Dot\", { radius:5 } ]\n" +
//"            };\n" +
//"\n" +
//"            var transportLinksToBeHighlighted = [" + transportLinksToBeHighlighted + "];\n\n" +
//"            //The non express connections\n" +
//"            var nonExpressTriplets = [\n" +
//"                   '6ad69dfc-c5e0-4691-be4f-17342b0b65c7','41033f44-f0e1-4566-8353-5353c7a6f264','4c9e49dd-c9ce-4908-aeda-1ab5cd2a1207',\n" +
//"                   '27cc23de-2257-4433-8e33-37b0339d6e7b','41033f44-f0e1-4566-8353-5353c7a6f264','061a2727-2bd4-4cd9-8d4a-eb8fdf35b8b6',\n" +
//"                   '5da7e887-5db3-4214-adde-d689cec7ecff','4c9e49dd-c9ce-4908-aeda-1ab5cd2a1207','a419df25-869c-46e2-b442-4ac567e52602',\n" +
//"                   '37d87077-f547-499c-9312-ead0bd5f5f86','a419df25-869c-46e2-b442-4ac567e52602','b76c88e7-1ee5-435b-b3d9-4f6bd2daa539',\n" +
//"                   'd7152bc0-1394-4f44-9f33-f0ab064e001f','14887543-7a5d-4825-bc15-d611b26a05aa','5ef4c2c3-e335-4794-964f-9c90f501ff8b',\n" +
//"                   '5c8a8cc1-8d93-4292-b32d-5a25a4e5401f','061a2727-2bd4-4cd9-8d4a-eb8fdf35b8b6','61efeab7-4b11-4f43-87a3-97709bcd6ff5',\n" +
//"                   '2e86153a-d118-477a-827e-7075d9bf7afe','c85bbb56-0cb6-4b15-89d8-990bc4e905d7','6f78855a-98b7-4494-82ce-ce2eaafbf2c1',\n" +
//"                   '99649c4a-224d-489f-8227-bf895dfd52d1','61efeab7-4b11-4f43-87a3-97709bcd6ff5','ce2963e4-c797-4a75-a240-29a143a906be',\n" +
//"                   'cc9e041f-fb17-49af-aa51-3e53f2551167','b76c88e7-1ee5-435b-b3d9-4f6bd2daa539','c85bbb56-0cb6-4b15-89d8-990bc4e905d7',\n" +
//"                   '16cd0cf4-0d36-428e-b71a-cda2375b6b4c','6f78855a-98b7-4494-82ce-ce2eaafbf2c1','ed45be7d-fe4a-41ea-aa51-041cde23e583',\n" +
//"                   'b22390e7-d663-4701-97dc-03113d4ea3b3','ce2963e4-c797-4a75-a240-29a143a906be','5ef4c2c3-e335-4794-964f-9c90f501ff8b',\n" +
//"                   '286e96d3-0932-4e89-a7e1-d85105981a93','ef0b7cc4-2c09-449c-974e-81c4fc383cb4','1522ff49-d068-4a6f-9eb2-7d3ef8582d94',\n" +
//"                   '63491e43-9adf-4624-8b3d-847189d0ed52','ed45be7d-fe4a-41ea-aa51-041cde23e583','ef0b7cc4-2c09-449c-974e-81c4fc383cb4',\n" +
//"                   '1f5878c5-edb3-4171-95b1-acfa725b6a5b','6bb250d5-3ec5-4367-aa5e-92023a9ba054','17c9bfd1-ae94-4bff-abfe-88366b1fa578',\n" +
//"                   '75ac751c-326e-4f55-b710-57e18810596d','1522ff49-d068-4a6f-9eb2-7d3ef8582d94','8eccdf31-2be2-47ee-af95-25d3e421b042',\n" +
//"                   'd48963fa-ec00-477f-b7ec-440d4fd994f6','8eccdf31-2be2-47ee-af95-25d3e421b042','eecfdefb-e224-4552-8f05-a12d6f0ed9c2',\n" +
//"                   '8092d637-7c8a-4375-b9a4-be127813c8d1','17c9bfd1-ae94-4bff-abfe-88366b1fa578','d92c10d3-bf1f-41af-bfa1-989f3b5880f7',\n" +
//"                   '25af29f1-2cd0-4283-9b77-31fcb9e00ab7','d92c10d3-bf1f-41af-bfa1-989f3b5880f7','6861314c-97fe-48c5-bc3b-920e24967920',\n" +
//"                   '220daeb9-6f5c-47a4-94df-b83ab53f02e2','47a80055-20f0-42ea-8eb4-d0fe61008ae4','b3b04665-a3a6-4e2a-a81f-d93d7497d9e8',\n" +
//"                   '450a2791-207b-430a-8637-d76d49320bec','6861314c-97fe-48c5-bc3b-920e24967920','47a80055-20f0-42ea-8eb4-d0fe61008ae4',\n" +
//"                   '4cf860dd-2432-43df-b97e-27ff3436b04b','b3b04665-a3a6-4e2a-a81f-d93d7497d9e8','14887543-7a5d-4825-bc15-d611b26a05aa'\n" +
//"            ];\n\n" +
//"            var nonExpressHighlighted = [];\n" +
//"            for (var i = 0; i < nonExpressTriplets.length; i = i + 3) {\n" +
//"                if (transportLinksToBeHighlighted.indexOf(nonExpressTriplets[i]) == -1)\n" +
//"                    jsPlumb.connect({\n" +
//"                        source: nonExpressTriplets[i + 1].toString(),\n" +
//"                        target: nonExpressTriplets[i + 2].toString(),\n" +
//"                        paintStyle:{ stroke:\"lightgray\", strokeWidth: 2  },\n" +
//"                        endpointStyle:{ fill:\"lightgray\", outlineStroke:\"lightgray\", outlineWidth:1 }\n" +
//"                    }, nonExpressConnections);\n" +
//"                else\n" +
//"                    nonExpressHighlighted.push({\n" +
//"                        source: nonExpressTriplets[i + 1].toString(),\n" +
//"                        target: nonExpressTriplets[i + 2].toString(),\n" +
//"                        paintStyle:{ stroke:\"red\", strokeWidth: 2  },\n" +
//"                        endpointStyle:{ fill:\"red\", outlineStroke:\"red\", outlineWidth:1 }\n" +
//"                    });\n" +
//"            }\n" +
//"\n" +
//"            //The express connections\n" +
//"            var expressTriplets = ['d788157e-dd57-4201-b2af-224c8d7944e5', '17c9bfd1-ae94-4bff-abfe-88366b1fa578', 'b3b04665-a3a6-4e2a-a81f-d93d7497d9e8',\n" +
//"                                 '31589b4f-30b4-4b2b-9ee0-f8644c00715e', 'b3b04665-a3a6-4e2a-a81f-d93d7497d9e8', '41033f44-f0e1-4566-8353-5353c7a6f264', \n" +
//"                                 '74d25ca4-12b8-4a44-a573-d468c3f8f66b', '41033f44-f0e1-4566-8353-5353c7a6f264', '6f78855a-98b7-4494-82ce-ce2eaafbf2c1', \n" +
//"                                 '930f9541-829d-4989-a7c9-e77967b4d8d8', '6f78855a-98b7-4494-82ce-ce2eaafbf2c1', '8eccdf31-2be2-47ee-af95-25d3e421b042'];\n\n" +
//"            var expressHighlighted = [];\n" +
//"            for (var i = 0; i < expressTriplets.length; i = i + 3) {\n" +
//"                if (transportLinksToBeHighlighted.indexOf(expressTriplets[i]) == -1)\n" +
//"                    jsPlumb.connect({\n" +
//"                        source: expressTriplets[i + 1].toString(),\n" +
//"                        target: expressTriplets[i + 2].toString(),\n" +
//"                        paintStyle:{ stroke:\"lightgray\", strokeWidth: 2  },\n" +
//"                        endpointStyle:{ fill:\"lightgray\", outlineStroke:\"lightgray\", outlineWidth: 1 }\n" +
//"                    }, expressConnections);\n" +
//"                else\n" +
//"                    expressHighlighted.push({\n" +
//"                        source: expressTriplets[i + 1].toString(),\n" +
//"                        target: expressTriplets[i + 2].toString(),\n" +
//"                        paintStyle:{ stroke:\"red\", strokeWidth: 2  },\n" +
//"                        endpointStyle:{ fill:\"red\", outlineStroke:\"red\", outlineWidth: 1 }\n" +
//"                    });\n" +
//"            }\n" +
//"            \n" +
//"            //The non-express connections that connect the endpoints using the bottom anchors\n" +
//"            var nonExpressNonDirectTriplets = ['de6e7126-c530-4789-95f9-c82b873f880e','6f78855a-98b7-4494-82ce-ce2eaafbf2c1','5e952532-b1a5-4dde-9219-a089e62b1aa7',\n" +
//"                                             '46db54dc-bdad-4930-baf3-1eb41f71832c','1522ff49-d068-4a6f-9eb2-7d3ef8582d94','9b00b7c5-0fa4-46db-8fc1-1e04082d4f94' ];\n" +
//"            var nonExpressNonDirectHighlighted = [];\n" +
//"            for (var i = 0; i < nonExpressNonDirectTriplets.length; i = i + 3) {\n" +
//"                if (transportLinksToBeHighlighted.indexOf(nonExpressNonDirectTriplets[i]) == -1)\n" +
//"                    jsPlumb.connect({\n" +
//"                        source: nonExpressNonDirectTriplets[i + 1].toString(),\n" +
//"                        target: nonExpressNonDirectTriplets[i + 2].toString(),\n" +
//"                        paintStyle:{ stroke:\"lightgray\", strokeWidth: 2  },\n" +
//"                        endpointStyle:{ fill:\"lightgray\", outlineStroke:\"lightgray\", outlineWidth: 1 }\n" +
//"                    }, extraConnections);\n" +
//"                else\n" +
//"                    nonExpressNonDirectHighlighted.push({\n" +
//"                        source: nonExpressNonDirectTriplets[i + 1].toString(),\n" +
//"                        target: nonExpressNonDirectTriplets[i + 2].toString(),\n" +
//"                        paintStyle:{ stroke:\"red\", strokeWidth: 2  },\n" +
//"                        endpointStyle:{ fill:\"red\", outlineStroke:\"red\", outlineWidth: 1 }\n" +
//"                    });\n" +
//"            }\n" + 
//"            for (var i = 0; i < nonExpressHighlighted.length; i++)\n" +
//"               jsPlumb.connect(nonExpressHighlighted[i], nonExpressConnections);\n" +
//"            for (var i = 0; i < expressHighlighted.length; i++)\n" +
//"               jsPlumb.connect(expressHighlighted[i], expressConnections);\n" +
//"            for (var i = 0; i < nonExpressNonDirectHighlighted.length; i++) \n" +
//"               jsPlumb.connect(nonExpressNonDirectHighlighted[i], extraConnections);\n" +
//"        });\n" +                    
//                    "</script>\n";
//            }
//            tributaryLinkUsageReportText += usedResources;
//            
//        }
//        tributaryLinkUsageReportText += getFooter();
//        return new RawReport("Tributary Link Details", "Neotropic SAS","1.2", tributaryLinkUsageReportText);
//    }
//    
    public RawReport buildNetworkEquipmentInLocationReport(String locationClass, String locationId) 
        throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, InvalidArgumentException {
                
        String title, networkEquipmentInLocationReportText;
        
        BusinessObjectLight location = bem.getObjectLight(locationClass, locationId);
        HashMap<String, String> filters = new HashMap<>();
        List<BusinessObjectLight> networkEquipment = bem.getChildrenOfClassLightRecursive(locationId, locationClass, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, 
                filters, -1, -1);
            
        title = "Network Equipment Report for " + location.getName();
        networkEquipmentInLocationReportText = getHeader(title);
        networkEquipmentInLocationReportText += 
                            "  <body><table><tr><td><h1>" + title + "</h1></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";

        networkEquipmentInLocationReportText += "<table><tr><td class=\"generalInfoLabel\">Name</td><td>" + location.getName() + "</td></tr>\n"
                + "<tr><td class=\"generalInfoLabel\">Type</td><td>" + location.getClassName() + "</td></tr>\n"
                + "<tr><td class=\"generalInfoLabel\">Location</td><td>" + Util.formatLocation(bem.getParents(location.getClassName(), location.getId())) + "</td></tr>\n</table>\n";

        if (networkEquipment.isEmpty())
            networkEquipmentInLocationReportText += "<div class=\"warning\">This location does not have any network equipment</div>";
        else {
            networkEquipmentInLocationReportText += "<table><tr><th>Name</th><th>Type</th><th>Serial Number</th><th>Location</th><th>Vendor</th><th>Operational State</th></tr>";
            int i = 0;
            for (BusinessObjectLight device : networkEquipment) {
                HashMap<String, String> deviceAttributes = bem.getAttributeValuesAsString(device.getClassName(), device.getId());
                networkEquipmentInLocationReportText += "<tr class=\"" + (i % 2 == 0 ? "even" :"odd") + "\">"
                                                            + "<td>" + device.getName() + "</td>"
                                                            + "<td>" + device.getClassName() + "</td>"
                                                            + "<td>" + (deviceAttributes.get("serialNumber") == null ? asError("Not Set") : deviceAttributes.get("serialNumber")) + "</td>"
                                                            + "<td>" + Util.formatLocation(bem.getParents(device.getClassName(), device.getId())) + "</td>"
                                                            + "<td>" + (deviceAttributes.get("vendor") == null ? asError("Not Set") : deviceAttributes.get("vendor") ) + "</td>"
                                                            + "<td>" + (deviceAttributes.get("state") == null ? asError("Not Set") : deviceAttributes.get("state") ) + "</td></tr>";
                i ++;
            }
            networkEquipmentInLocationReportText += "</table>";
        }
        
        
        networkEquipmentInLocationReportText += getFooter();
        
        return new RawReport("Network Equipment", "Neotropic SAS","1.1", networkEquipmentInLocationReportText);
    }
//    
//    public RawReport buildServiceResourcesReport(String className, String serviceId) 
//        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, InvalidArgumentException {
//        BusinessObjectLight service = bem.getObjectLight(className, serviceId);
//        String serviceResourcesReportText, title = "Resources Used By " + service.getName();
//        serviceResourcesReportText = getHeader(title);
//        serviceResourcesReportText += 
//                            "  <body><table><tr><td><h1>" + title + "</h1></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";
//
//        serviceResourcesReportText += "<table><tr><td class=\"generalInfoLabel\">Name</td><td>" + service.getName() + "</td></tr>\n"
//                + "<tr><td class=\"generalInfoLabel\">Type</td><td>" + service.getClassName() + "</td></tr>\n"
//                + "<tr><td class=\"generalInfoLabel\">Location</td><td>" + Util.formatLocation(bem.getParents(service.getClassName(), service.getId())) + "</td></tr>\n</table>\n";
//        List<BusinessObjectLight> resources = bem.getSpecialAttribute(service.getClassName(), service.getId(), "uses");
//        if (resources.isEmpty()) {
//            serviceResourcesReportText += "<div class=\"warning\">This service does not use any network resources</div>";
//        } else {
//            serviceResourcesReportText += "<table><tr><th>Name</th><th>Type</th><th>Location</th></tr>";
//            int i = 0;
//            for (BusinessObjectLight resource : resources) {
//                serviceResourcesReportText += "<tr class=\"" + (i % 2 == 0 ? "even" :"odd") + "\">"
//                                                            + "<td>" + resource.getName() + "</td>"
//                                                            + "<td>" + resource.getClassName() + "</td>"
//                                                            + "<td>" + Util.formatLocation(bem.getParents(resource.getClassName(), resource.getId())) + "</td></tr>";
//                i ++;
//            }
//            serviceResourcesReportText += "</table>";
//        }
//        
//        serviceResourcesReportText += getFooter();
//        return new RawReport("Service Resources", "Neotropic SAS","1.1", serviceResourcesReportText);
//    }
//    
//    public RawReport subnetUsageReport(String className, String subnetId) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
//    
//        BusinessObject subnet = bem.getObject(className, subnetId);
//        List<BusinessObjectLight> subnetChildren = bem.getObjectSpecialChildren(className, subnetId);
//        HashMap<String, String> subnetAttributes = subnet.getAttributes();
//        int hosts = Integer.parseInt(subnetAttributes.get("hosts"));
//        int usedIps = 0;
//        List<BusinessObjectLight> ips  = new ArrayList<>();
//        List<BusinessObjectLight> subnets  = new ArrayList<>();
//        
//        for (BusinessObjectLight children : subnetChildren) {
//            if(children.getClassName().equals(Constants.CLASS_IP_ADDRESS))
//                ips.add(children);
//        }
//        
//        for (BusinessObjectLight children : subnetChildren) {
//            if(children.getClassName().equals(Constants.CLASS_SUBNET_IPV4) || children.getClassName().equals(Constants.CLASS_SUBNET_IPV6))
//                subnets.add(children);
//        }
//        
//        for (BusinessObjectLight ip : ips) {
//            List<BusinessObjectLight> ipDevices = bem.getSpecialAttribute(Constants.CLASS_IP_ADDRESS, ip.getId(), IPAMModule.RELATIONSHIP_IPAMHASADDRESS);
//            if(!ipDevices.isEmpty())
//                usedIps++;
//        }
//        // There are not host but the
//        // gateway and the broadcast
//        // are in use
//        if (hosts == 0 && usedIps == 0){
//            usedIps = ips.size();
//            if (usedIps>hosts)
//                hosts += 2;
//        }
//        int freeIps = hosts - usedIps;
//        
//        String vrf="", vlan = "", service="", title, subnetUsageReportText;
//        List<BusinessObjectLight> vlans = bem.getSpecialAttribute(className, subnetId, IPAMModule.RELATIONSHIP_IPAMBELONGSTOVLAN);
//        List<BusinessObjectLight> vrfs = bem.getSpecialAttribute(className, subnetId, IPAMModule.RELATIONSHIP_IPAMBELONGSTOVRFINSTACE);
//        List<BusinessObjectLight> services = bem.getSpecialAttribute(className, subnetId, "uses");
//        
//        if(!vlans.isEmpty())
//            vlan = "<b>" + vlans.get(0).getName() + " ["+ vlans.get(0).getClassName()+ "]</b> |"+
//            Util.formatLocation(bem.getParents(vlans.get(0).getClassName(), vlans.get(0).getId()));
//        
//        if(!vrfs.isEmpty())
//            vrf = "<b>" + vrfs.get(0).getName() + " ["+ vrfs.get(0).getClassName()+ "]</b>";
//
//        if(!services.isEmpty())
//            service = services.get(0).getName() + " ["+ services.get(0).getClassName()+ "]";
//        
//        if (subnet == null) {
//            title = "Error";
//            subnetUsageReportText = getHeader(title);
//            subnetUsageReportText += "<div class=\"error\">No information about this subnet could be found</div>";
//        }
//        else {
//            title = "Subnet Usage Detail Report for " + subnet.getName();
//            subnetUsageReportText = getHeader(title);
//            subnetUsageReportText += 
//                                "  <body><table><tr><td><h1>" + title + "</h1></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";
//            
//            subnetUsageReportText += pieChartScript(usedIps, freeIps);
//
//            subnetUsageReportText += "<table><tr><td class=\"generalInfoLabel\">Network IP Addres</td><td class=\"generalInfoValue\"><b>" + subnetAttributes.get("networkIp") + "</b></td>"
//                    + "<td rowspan=\"8\"><div id=\"piechart\" style=\"width: 350px; height: 250px;\"></div></td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Broadcast IP Address</td><td class=\"generalInfoValue\"><b>" + subnetAttributes.get("broadcastIp") + "</b> </td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Description </td><td class=\"generalInfoValue\">" + subnetAttributes.get("description") + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Number of hosts</td><td class=\"generalInfoValue\">" + hosts + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">IPs Related to some port</td><td class=\"generalInfoValue\"><b>" + (usedIps*100)/hosts + "%</b> ("+ usedIps +")</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Free IPs</td><td class=\"generalInfoValue\"><b>" + (freeIps*100)/hosts +"%</b> ("+ freeIps +")</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">VLAN</td><td class=\"generalInfoValue\">" + vlan + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">VRF</td><td class=\"generalInfoValue\">" + vrf + "</td></tr>"
//                    + "<tr><td class=\"generalInfoLabel\">Service</td><td class=\"generalInfoValue\">" + service + "</td></tr></table>";
//        }
//        
//        String nestedSubnets; 
//        String ipAddresses; 
//        
//        if (subnets.isEmpty())
//            nestedSubnets = "<div class=\"error\">There are no Subnets nested</div>";
//        else {
//            nestedSubnets = "<br><h2>Subnets</h2><table><tr><th>Subnet</th><th>Description</th><th>Service</th></tr>";
//
//            int i = 0;
//            for (BusinessObjectLight nestedSubnet : subnets) {
//                service = "";
//                                
//                List<BusinessObjectLight> subnetServices = bem.getSpecialAttribute(nestedSubnet.getClassName(), nestedSubnet.getId(), "uses"); //NOI18N
//                if(!subnetServices.isEmpty())
//                    service = subnetServices.get(0).getName() + "[" +  subnetServices.get(0).getClassName() + "]";
//                
//                BusinessObject subnetO = bem.getObject(className, nestedSubnet.getId());
//                HashMap<String, String> attributes = subnetO.getAttributes();
//                
//                nestedSubnets += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd") +"\"><td>" + nestedSubnet.getName() + "</td>"
//                              + "<td>" + attributes.get("description") +"</td>"
//                              + "<td>" + service +"</td></tr>";
//                i ++;
//            }
//            nestedSubnets += "</table>";
//        }
//        
//        subnetUsageReportText += nestedSubnets;
//        
//        if (ips.isEmpty())
//            ipAddresses = "<div class=\"error\">There are no IPs Addresses in use</div>";
//        else {
//            ipAddresses = "<br><h2>IP Addresses</h2><table><tr><th>IP Address</th><th>Description</th><th>Port</th><th>Location</th><th>Service</th></tr>";
//
//            int i = 0;
//            for (BusinessObjectLight ip : ips) {
//                String device = "";
//                service = "";
//                
//                List<BusinessObjectLight> ipDevices = bem.getSpecialAttribute(Constants.CLASS_IP_ADDRESS, ip.getId(), IPAMModule.RELATIONSHIP_IPAMHASADDRESS);
//                String location = "";
//                if(!ipDevices.isEmpty()){
//                    device = ipDevices.get(0).getName() + " [" + ipDevices.get(0).getClassName()+"]";
//                    List<BusinessObjectLight> parents = bem.getParents(ipDevices.get(0).getClassName(), ipDevices.get(0).getId());
//                    location =  Util.formatLocation(parents);
//                }
//                
//                List<BusinessObjectLight> ipServices = bem.getSpecialAttribute(Constants.CLASS_IP_ADDRESS, ip.getId(), "uses");
//                if(!ipServices.isEmpty())
//                    service = ipServices.get(0).getName() + "[" +  ipServices.get(0).getClassName() + "]";
//                
//                BusinessObject ipO = bem.getObject(Constants.CLASS_IP_ADDRESS, ip.getId());
//                HashMap<String, String> attributes = ipO.getAttributes();
//                
//                ipAddresses += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd") +"\"><td>" + ip.getName() + "</td>"
//                              + "<td>" + attributes.get("description") +"</td>"
//                              + "<td>" + device +"</td>"
//                              + "<td>" + location +"</td>"
//                              + "<td>" + service +"</td></tr>";
//                i ++;
//            }
//            ipAddresses += "</table>";
//        }
//        
//        subnetUsageReportText += ipAddresses;
//        subnetUsageReportText += getFooter();
//        
//        return new RawReport("Subnet Usage", "Neotropic SAS", "1.1", subnetUsageReportText);
//    }
//    
//    public RawReport buildContractStatusReport() throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
//        
//        List<BusinessObjectLight> contracts = bem.getObjectsOfClassLight(Constants.CLASS_GENERICCONTRACT, 0);
//        
//        String title = "Contract Status Report";
//        String contractStatusReportText = getHeader(title);
//        
//        contractStatusReportText += 
//                            "  <body><table><tr><td><h1>" + title + "</h1></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";
//        
//        if (contracts.isEmpty())
//            contractStatusReportText += "<div class=\"warning\">This pool does not have contracts attached</div>";
//        else {
//            contractStatusReportText += "<table><tr><th>Name</th><th>Start Date</th><th>Expiration Date</th><th>Equipment</th><th>Provider</th><th>Phone Number</th><th>Email</th></tr>";
//            
//            int i = 0;
//            SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy");
//            for (BusinessObjectLight aContract : contracts) {
//                BusinessObject fullContractInfo = bem.getObject(aContract.getClassName(), aContract.getId());
//                Date startDate =  fullContractInfo.getAttributes().get("startDate") == null ? 
//                        null : new Date(Long.valueOf(fullContractInfo.getAttributes().get("startDate")));
//                
//                String startDateString;
//                
//                if (startDate == null) 
//                    startDateString = asError("Not Set");
//                else 
//                    startDateString = formatter.format(startDate);
//                                
//                String expirationDateString;
//                Date expirationDate =  fullContractInfo.getAttributes().get("expirationDate") == null ? 
//                        null : new Date(Long.valueOf(fullContractInfo.getAttributes().get("expirationDate")));
//                
//                if (expirationDate == null)
//                    expirationDateString = asError("Not Set");
//                else {
//                    Calendar calendar = Calendar.getInstance();
//                    calendar.add(Calendar.DAY_OF_YEAR, 60);
//                    Date withinTwoMonths = calendar.getTime();
//                    
//                    if (expirationDate.compareTo(withinTwoMonths) > 0)
//                        expirationDateString = formatter.format(expirationDate);
//                    else {
//                        calendar.add(Calendar.DAY_OF_YEAR, -30);
//                        Date withinAMonth = calendar.getTime();
//                        if (expirationDate.compareTo(withinAMonth) > 0)
//                            expirationDateString = asWarning(formatter.format(expirationDate));
//                        else
//                            expirationDateString = asError(formatter.format(expirationDate));
//                    }
//                }
//                
//                List<BusinessObjectLight> equipment = bem.getSpecialAttribute(aContract.getClassName(), aContract.getId(), "contractHas"); //NOI18N
//                
//                String equipmentString = "";
//                if (equipment.isEmpty())
//                    equipmentString = asError("No Equipment");
//                else {
//                    for (BusinessObjectLight anEquipment : equipment)
//                        equipmentString += anEquipment + "<br/>";
//                }
//                
//                String providerName = asError("Not Set");
//                String providerPhoneNumber = asError("Not Set");
//                String providerEmail = asError("Not Set");
//                
//                if (fullContractInfo.getAttributes().containsKey("serviceProvider")) {
//                    BusinessObject serviceProvider = bem.getObject(Constants.CLASS_SERVICEPROVIDER, fullContractInfo.getAttributes().get("serviceProvider"));
//                    if (!serviceProvider.getName().isEmpty())
//                        providerName = serviceProvider.getName();
//                    if (serviceProvider.getAttributes().get(Constants.PROPERTY_SUPPORT_PHONE_NUMBER) != null)
//                        providerPhoneNumber = serviceProvider.getAttributes().get(Constants.PROPERTY_SUPPORT_PHONE_NUMBER);
//                    if (serviceProvider.getAttributes().get(Constants.PROPERTY_SUPPORT_EMAIL) != null)
//                        providerEmail = serviceProvider.getAttributes().get(Constants.PROPERTY_SUPPORT_EMAIL);
//                }
//                
//                contractStatusReportText += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd") + "\"><td>" + aContract.getName() + "</td>"
//                                                + "<td>" + startDateString + "</td><td>" + expirationDateString + "</td><td>" + equipmentString 
//                                                + "</td><td>" + providerName + "</td><td>" + providerPhoneNumber + "</td><td>" + providerEmail + "</td></tr>\n";
//                i ++;
//            }
//            contractStatusReportText += "</table>";
//        }
//        
//        contractStatusReportText += getFooter();
//        
//        return new RawReport("Contract About to Expire", "Neotropic SAS", "1.1", contractStatusReportText);
//    }
//    
//    public RawReport buildMPLSServiceReport(String serviceClass, String serviceId) 
//            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException,
//            InvalidArgumentException, ApplicationObjectNotFoundException, 
//            NotAuthorizedException
//    {
//        BusinessObject MPLSService = bem.getObject(serviceClass, serviceId);
//        List<BusinessObjectLight> serviceInstances = bem.getSpecialAttribute(serviceClass, serviceId, "uses");
//
//        String service="", title, MPLSDetailReportText;
//        
//        if (MPLSService == null) {
//            title = "Error";
//            MPLSDetailReportText = getHeader(title);
//            MPLSDetailReportText += "<div class=\"error\">No information about this MPLS service could be found</div>";
//        }
//        else {
//            title = "MPLS service detail Report for " + MPLSService.getName() + "[" + MPLSService.getClassName() + "]";
//            MPLSDetailReportText = getHeader(title);
//            MPLSDetailReportText += 
//                                "  <body><table><tr><td><h1>" + title + "</h1></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";
//        }
//            String instance; 
//            
//            if (serviceInstances.isEmpty())
//                instance = "<div class=\"error\">There are no instance asossiate to this service</div>";
//            else {
//                instance = "<table><tr><th>Service Instance</th><th>Location</th></tr>";
//
//            int i = 0;
//            for (BusinessObjectLight serviceInstance : serviceInstances) {
//                String device = "";
//                service = "";
//                
//                BusinessObject ports = bem.getObject(serviceInstance.getClassName(), serviceInstance.getId());
//                String location = "";
//                if(ports != null){
//                    device = ports.getName() + " [" + ports.getClassName()+"]";
//                    List<BusinessObjectLight> parents = bem.getParents(ports.getClassName(), ports.getId());
//                    location =  Util.formatLocation(parents);
//                }
//                
//                instance += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd") +"\"><td>" + serviceInstance.getName() + " [" + serviceInstance.getClassName()+"] </td>"
//                              + "<td>" + location +"</td>";
//                              
//                i ++;
//            }
//            instance += "</table>";
//        }
//        MPLSDetailReportText += instance;
//        MPLSDetailReportText += getFooter();
//        
//        return new RawReport("MPLS Service Detail", "Neotropic SAS", "1.1", MPLSDetailReportText);
//    }
//    
    public RawReport buildLogicalConfigurationInterfacesReport(String logicalConfigurationClassName) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException,
            InvalidArgumentException, ApplicationObjectNotFoundException, 
            NotAuthorizedException
    {
        String title, DetailReportText = "", instance = "", vlan = "  "; 
        
        title = "Detail Report for all " + logicalConfigurationClassName + " instances";
        DetailReportText = getHeader(title);
        DetailReportText += "<body><table><tr><td><h1>" + title + "</h1></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";
        
        List<BusinessObjectLight> listOflogicalConfigurations = bem.getObjectsOfClassLight(logicalConfigurationClassName, null, -1, -1);
        
        for (BusinessObjectLight listOflogicalConfiguration : listOflogicalConfigurations) {
            BusinessObject logicalConfigurationObject = bem.getObject(listOflogicalConfiguration.getClassName(), listOflogicalConfiguration.getId());
            
            HashMap<String, Object> attributes = logicalConfigurationObject.getAttributes();
            List<BusinessObjectLight> ports = bem.getSpecialAttribute(listOflogicalConfiguration.getClassName(), listOflogicalConfiguration.getId(), RELATIONSHIP_IPAMPORTRELATEDTOINTERFACE);
            
            if (ports == null) 
                DetailReportText += "<div class=\"error\">No information for" + listOflogicalConfiguration.getName() + " could be found</div>";
            
            
            else {
                DetailReportText += "<table><tr><td class=\"generalInfoLabel\">Name</td><td class=\"generalInfoValue\"><b>" + logicalConfigurationObject.getName() + "[" + logicalConfigurationObject.getClassName() + "]</b></td>";
                List<BusinessObjectLight> vlans = bem.getSpecialAttribute(listOflogicalConfiguration.getClassName(), listOflogicalConfiguration.getId(), RELATIONSHIP_IPAMBELONGSTOVLAN);
                if (!vlans.isEmpty()){
                    for (BusinessObjectLight vlanInstance : vlans) 
                        vlan += vlanInstance.toString() + ", ";  
                    DetailReportText +=  "<tr><td class=\"generalInfoLabel\">VLAN</td><td class=\"generalInfoValue\"><b>" + vlan.substring(0, vlan.length()-2) + "</b> </td></tr>";
                }
                DetailReportText +=  "<tr><td class=\"generalInfoLabel\">Creation date</td><td class=\"generalInfoValue\"><b>" + new Date((Long) attributes.get("creationDate")) + "</b> </td></tr></table>";
            }

            if (ports.isEmpty())
                    instance = "<div class=\"error\">There is nothing related to " + listOflogicalConfiguration.toString() +"</div>";
            else {
                    instance = "<table><tr><th>Port / Device</th><th>IP Address</th><th>Device Location</th></tr>";

                int i = 0;
                for (BusinessObjectLight relatedPort : ports) {
                    List<BusinessObjectLight> ipAddresses = bem.getSpecialAttribute(relatedPort.getClassName(), relatedPort.getId(), RELATIONSHIP_IPAMHASADDRESS);
                    BusinessObject port = bem.getObject(relatedPort.getClassName(), relatedPort.getId());
                    String location = "";
                    if(port != null) {
                        List<BusinessObjectLight> parents = bem.getParents(port.getClassName(), port.getId());
                        location =  Util.formatLocation(parents);
                    }

                    String ips = "  ";
                    for (BusinessObjectLight ipAddress : ipAddresses) 
                        ips += ipAddress.getName() + ", ";
                    
                    
                    instance += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd")
                            + "\"><td>" + relatedPort.getName() + " [" + relatedPort.getClassName()+"] </td>"
                            + "<td>" + ips.substring(0, ips.length()-2) +"</td>"
                            + "<td>" + location +"</td>";

                    i ++;
                }
                instance += "</table>";
            }
            
            DetailReportText += instance;
            DetailReportText += "<hr>";
        }
        DetailReportText = DetailReportText.substring(0, DetailReportText.length()-4);
        
        DetailReportText += getFooter();
        
        return new RawReport("Logical Configuration", "Neotropic SAS", "1.1", DetailReportText);
    }
//    
//    public RawReport buildServicesReport(String serviceClassName, String serviceId) 
//            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException,
//            InvalidArgumentException, ApplicationObjectNotFoundException, 
//            NotAuthorizedException
//    {
//        String title, ServiceDetailReportText;
//        BusinessObject theService = bem.getObject(serviceClassName, serviceId);
//        
//        if (theService == null) {
//            title = "Error";
//            ServiceDetailReportText = getHeader(title);
//            ServiceDetailReportText += "<div class=\"error\">No information about this service could be found</div>";
//        }
//        else {
//            HashMap<String, String> serviceAttributes = theService.getAttributes();
//            List<AttributeMetadata> serviceClassAttributes = mem.getClass(serviceClassName).getAttributes();
//            title = "Service detail Report for " + theService;
//            ServiceDetailReportText = getHeader(title);
//            ServiceDetailReportText += "<body>"
//                    + "<table><tr><td><h2>" + title +"</h2></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";
//
//            ServiceDetailReportText += "<table>";
//            List<BusinessObjectLight> parents = bem.getParents(serviceClassName, serviceId);
//            
//            BusinessObject serviceCustomer = null;
//                    
//            for (BusinessObjectLight parent : parents) {
//                if(mem.isSubclassOf(Constants.CLASS_GENERICCUSTOMER, parent.getClassName())){
//                    serviceCustomer = bem.getObject(parent.getClassName(), parent.getId());
//                    break;
//                }
//            }
//            
//            HashMap<String, String> customerAttributes = serviceCustomer.getAttributes();
//            ClassMetadata customerClass = mem.getClass(serviceCustomer.getClassName());
//            List<AttributeMetadata> customerClassAttributes = customerClass.getAttributes();
//            
//            ServiceDetailReportText += createAttributesOfClass(serviceAttributes, serviceClassAttributes);
//            ServiceDetailReportText += "<table><tr><td><h2>Customer Details: "+serviceCustomer.toString()+"</h1></td><td></td></tr></table>\n"
//                    + "<table>";
//            ServiceDetailReportText += createAttributesOfClass(customerAttributes, customerClassAttributes);
//            
//            ServiceDetailReportText += "</table>";
//
//        }
//        
//        List<BusinessObjectLight> serviceInstances = bem.getSpecialAttribute(serviceClassName, serviceId, "uses");
//        String instance; 
//
//        if (serviceInstances.isEmpty())
//            instance = "<div class=\"error\">There are no service instances associated to this service</div>";
//        else {
//            instance = "<table><tr><th>Related Instances</th><th>Location</th></tr>";
//
//            int i = 0;
//            for (BusinessObjectLight serviceInstance : serviceInstances) {
//
//                BusinessObject inventoryObject = bem.getObject(serviceInstance.getClassName(), serviceInstance.getId());
//                String location = "";
//                if(inventoryObject != null) {
//                    List<BusinessObjectLight> parents = bem.getParents(inventoryObject.getClassName(), inventoryObject.getId());
//                    location = Util.formatLocation(parents);
//                }
//
//                instance += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd") +"\"><td>" + serviceInstance + "</td>"
//                              + "<td>" + location +"</td>";
//
//                i ++;
//            }
//            instance += "</table>";
//        }
//        ServiceDetailReportText += instance;
//        ServiceDetailReportText += getFooter();
//        
//        return new RawReport("Service Details", "Neotropic SAS", "1.1", ServiceDetailReportText);
//    }
    
    //<editor-fold desc="Helpers" defaultstate="collapsed">
    private String getStyleSheet() {
        return "<style> " +
                    "   @import url('https://fonts.googleapis.com/css?family=Open+Sans');\n"+
                    "   body {\n" +
                    "            font-family: 'Open Sans', sans-serif;\n" +
                    "            font-size: small;\n" +
                    "            padding: 5px 10px 5px 10px;\n" +
                    "            color: #00445C;\n"+
                    "   }\n" +
                    "   table {\n" +
                    "            border: hidden;\n" +
                    "            width: 100%;\n" +
                    "          }\n" +
                    "   th {\n" +
                    "            background-color: #88AA00;\n" +
                    "            padding: 7px 7px 7px 7px;\n" +
                    "            color: white;\n" +
                    "            font-weight: normal;\n" +
                    "   }\n" +
                    "   td {\n" +
                    "            padding: 7px 7px 7px 7px;\n" +
                    "   }\n" +
                    "   div {\n" +
                    "            padding: 5px 5px 5px 5px;\n" +
                    "   }\n" +
                    "   div.warning {\n" +
                    "            background-color: #FFF3A2;\n" +
                    "            text-align: center;\n" +
                    "   }\n" +
                    "   div.error {\n" +
                    "            background-color: #FFD9C7;\n" +
                    "            text-align: center;\n" +
                    "   }\n" +
                    "   div.footer {\n" +
                    "            width: 100%;\n" +
                    "            text-align: center;\n" +
                    "            font-style: italic;\n" +
                    "            font-size: x-small;\n" +
                    "            color: #848484;\n" +
                    "   }\n" +
                    "   span.ok {\n" +
                    "            color: green;\n" +
                    "   }\n" +
                    "   span.warning {\n" +
                    "            color: orange;\n" +
                    "   }\n" +
                    "   span.error {\n" +
                    "            color: red;\n" +
                    "   }\n" +
                    "   td.generalInfoLabel {\n" +
                    "            background-color: #FF9167;\n" +
                    "            width: 20%;\n" +
                    "   }\n" +
                    "   td.generalInfoValue {\n" +
                    "            background-color: white;\n" +
                    "   }\n" +
                    "   tr.even {\n" +
                    "            background-color: #FFD059;\n" +
                    "   }\n" +
                    "   tr.odd {\n" +
                    "            background-color: #fff;\n" +
                    "   }" +
                    "   hr { \n" +
                    "            display: block; \n"+
                    "            margin-top: 0.5em; \n"+
                    "            margin-bottom: 0.5em; \n"+
                    "            margin-left: auto; \n"+
                    "            margin-right: auto; \n"+
                    "            border-style: inset; \n"+
                    "            border-width: 1px; \n"+
                    "            color: #A5DF00; \n"+
                    "       }  \n"+
                    "</style>\n";
    }
    
    private String getHeader(String title){
        return "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "  <head>\n" +
                    "    <meta charset=\"utf-8\">\n" +
                    "    <title>" + title + "</title>\n" +
                    getStyleSheet() +
                    "  </head>\n";
    }
    
    private String getFooter() {
        return "  <div class=\"footer\">This report is powered by <a href=\"http://www.kuwaiba.org\">Kuwaiba Open Network Inventory</a></div></body>\n" +
                                "</html>";
    }
    
    private String asOk(String text) {
        return "<span class=\"ok\">" + text + "</span>";
    }
    
    private String asWarning(String text) {
        return "<span class=\"warning\">" + text + "</span>";
    }
    
    private String asError(String text) {
        return "<span class=\"error\">" + text + "</span>";
    }
    
    private String pieChartScript(int usedIps, int freeIps){
        String script = "\n<script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>\n" +
                        "    <script type=\"text/javascript\">\n" +
                        "      google.charts.load('current', {'packages':['corechart']});\n" +
                        "      google.charts.setOnLoadCallback(drawChart);\n" +
                        "      function drawChart() {\n" +
                        "        var data = google.visualization.arrayToDataTable\n" +
                        "                      ([\n"+
                        "                      ['IP', 'Usage %'],\n" +
                        "                      ['Used'," + usedIps + "],\n"+
                        "                      ['Free'," + freeIps + "]\n" +
                        "                   ])\n"+
                        "        var options = {title: 'Subnet Usage'};\n"+
                        "        var chart = new google.visualization.PieChart(document.getElementById('piechart'));\n"+
                        "        chart.draw(data, options);\n"+
                        "       }\n"+
                        "</script>\n";
        return script;
    }
    
    private String asKLM(int position) {
            int k, l, m;
            
            if (position % 21 == 0) {
                k = position / 21;
                l = 7;
                m = 3;
            } else {
                k = (position / 21) + 1;
                if ((position % 21) % 3 == 0)
                    l = (position % 21) / 3;
                else 
                    l = (position % 21) / 3 + 1;
                
                if ((position % 21) % 3 == 0)
                    m = 3;
                else
                    m = (position % 21) % 3;
            }
            
            return String.format("%s [%s - %s - %s]", position, k, l, m);
        }
    
    private String createAttributesOfClass(HashMap<String, String> attributes, List<AttributeMetadata> classAttributes) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, 
            InvalidArgumentException, ApplicationObjectNotFoundException, 
            NotAuthorizedException {
        String ServiceDetailReportText = "";
        for (AttributeMetadata a : classAttributes) {
            String valueAsString = attributes.get(a.getName());
            String valueToPrint = "";
            if(valueAsString != null){
                if(!AttributeMetadata.isPrimitive(a.getType())) //It's a list type
                    valueToPrint = aem.getListTypeItem(a.getType(), valueAsString).toString();
                else if(a.getType().equals("Date"))
                    valueToPrint = new Date(Long.valueOf(valueAsString)).toString();
                else 
                    valueToPrint = valueAsString;
                
            }
            
            ServiceDetailReportText += "<tr><td class=\"generalInfoLabel\"><b></b>" + a.getName() + 
                    "</b></td><td class=\"generalInfoValue\"><b>" + valueToPrint + "</b></td></tr>";
        }
        return ServiceDetailReportText;
    }
////    //</editor-fold> 
}
