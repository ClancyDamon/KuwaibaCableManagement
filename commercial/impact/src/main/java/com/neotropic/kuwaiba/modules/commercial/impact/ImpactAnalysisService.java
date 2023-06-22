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
package com.neotropic.kuwaiba.modules.commercial.impact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implements the functionality offered by {@link ImpactAnalysisModule}.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class ImpactAnalysisService {
    /*
    * Translation service
    */
    @Autowired
    private TranslationService ts;
    /**
     * The MetadataEntityManager instance
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * The BusinessEntityManager instance
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * The ApplicationEntityManager instance
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the service that provides methods to manipulate physical connections. 
     */
    @Autowired
    private PhysicalConnectionsService physicalConnectionsService;
    
    public AssetLevelCorrelatedInformation servicesInDevice(String deviceName) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, ApplicationObjectNotFoundException {
        List<BusinessObject> matchedCommunicationsElements = bem.getObjectsWithFilter("GenericCommunicationsElement", "name", deviceName);
                        
        if (matchedCommunicationsElements.isEmpty())
            throw new BusinessObjectNotFoundException(String.format("No resource with name %s could be found", deviceName));

        List<BusinessObjectLight> rawServices = new ArrayList<>();
        for (BusinessObject matchedCommunicationsElement : matchedCommunicationsElements) {

            //First the directly related
            List<BusinessObjectLight> directlyAssociatedServices = 
                    bem.getSpecialAttribute(matchedCommunicationsElement.getClassName(), matchedCommunicationsElement.getId(), "uses");

            rawServices.addAll(directlyAssociatedServices);

            //Now those associated to the ports
            List<BusinessObjectLight> portsInRouter = 
                    bem.getChildrenOfClassLightRecursive(matchedCommunicationsElement.getId(), 
                            matchedCommunicationsElement.getClassName(), "GenericPort", null, -1, -1); //NOI18N

            for (BusinessObjectLight portInRouter : portsInRouter) {
                List<BusinessObjectLight> rawServicesInPort = bem.getSpecialAttribute(portInRouter.getClassName(), portInRouter.getId(), "uses");
                
                rawServicesInPort.forEach(serviceInPort -> {
                    if (!rawServices.contains(serviceInPort))
                        rawServices.add(serviceInPort);
                });

                List<BusinessObjectLight> physicalPath = physicalConnectionsService.getPhysicalPath(portInRouter.getClassName(), portInRouter.getId());
                if (physicalPath.size() > 1) {
                    List<BusinessObjectLight> rawServicesInPhysicalConnection = bem.getSpecialAttribute(physicalPath.get(1).getClassName(), physicalPath.get(1).getId(), "uses");
                    rawServicesInPhysicalConnection.forEach(serviceInPhysicalConnection -> {
                        if (!rawServices.contains(serviceInPhysicalConnection))
                            rawServices.add(serviceInPhysicalConnection);
                    });
                }
            }
        }
        
        List<ServiceLevelCorrelatedInformation> serviceLevelCorrelatedInformation = new ArrayList<>();
        HashMap<BusinessObjectLight, List<BusinessObjectLight>> rawCorrelatedInformation = new HashMap<>();

        //Now we organize the rawServices by customer
        for (BusinessObjectLight rawService : rawServices) {
            BusinessObjectLight customer = bem.getFirstParentOfClass(rawService.getClassName(), rawService.getId(), "GenericCustomer");
            if (customer != null) { //Services without customers will be ignored. This shouldn't happen, though
                if (!rawCorrelatedInformation.containsKey(customer))
                    rawCorrelatedInformation.put(customer, new ArrayList());
 
                rawCorrelatedInformation.get(customer).add(rawService);
            }
        }

        rawCorrelatedInformation.keySet().forEach(customer -> { 
            serviceLevelCorrelatedInformation.add(new ServiceLevelCorrelatedInformation(customer, rawCorrelatedInformation.get(customer)));
        });

        return new AssetLevelCorrelatedInformation(matchedCommunicationsElements, serviceLevelCorrelatedInformation);
    }

    public AssetLevelCorrelatedInformation servicesInSlotOrBoard(String deviceName, String childName) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, ApplicationObjectNotFoundException {
        List<BusinessObject> matchedCommunicationsElements = bem.getObjectsWithFilter("GenericCommunicationsElement", "name", deviceName);
                        
        if (matchedCommunicationsElements.isEmpty())
            throw new BusinessObjectNotFoundException(String.format("No resource with name %s could be found", deviceName));
        
        if (matchedCommunicationsElements.size() > 1)
            throw new InvalidArgumentException(String.format("There is more than one network element with name %s", deviceName));
        
        List<BusinessObject> fullPortsInSlot = new ArrayList<>();

        List<BusinessObjectLight> deviceChildren = bem.getObjectChildren(matchedCommunicationsElements.
                                                                    get(0).getClassName(), matchedCommunicationsElements.get(0).getId(), -1);
            
        for (BusinessObjectLight deviceChild : deviceChildren) {
            if (childName.equals(deviceChild.getName())) {
                List<BusinessObjectLight> portsInSlot = bem.getObjectChildren(deviceChild.getClassName(), deviceChild.getId(), -1);
                
                for (BusinessObjectLight portInSlot : portsInSlot) {
                    if (mem.isSubclassOf("GenericPort", portInSlot.getClassName())) 
                        fullPortsInSlot.add(bem.getObject(portInSlot.getClassName(), portInSlot.getId()));
                }
                return servicesInPorts(fullPortsInSlot);
            }
        }  
        
        throw new BusinessObjectNotFoundException("No slot or port with name " + childName + " was found in device " + deviceName);
    }
    
    public AssetLevelCorrelatedInformation servicesInPorts(List<BusinessObject> ports) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException {
        List<BusinessObjectLight> rawServices = new ArrayList<>();
        
        for (BusinessObject port : ports) {
            List<BusinessObjectLight> servicesInPort = bem.getSpecialAttribute(port.getClassName(), port.getId(), "uses");
            rawServices.addAll(servicesInPort);

            List<BusinessObjectLight> physicalPath = physicalConnectionsService.getPhysicalPath(port.getClassName(), port.getId());
            if (physicalPath.size() > 1) { // The port is connected to something else, we'll check if the cable/fiber connected to it is associated to a service
                List<BusinessObjectLight> servicesInPhysicalConnection = bem.getSpecialAttribute(physicalPath.
                        get(1).getClassName(), physicalPath.get(1).getId(), "uses");
                servicesInPhysicalConnection.forEach( aService -> {
                    if (!rawServices.contains(aService))
                        rawServices.add(aService);
                });
            }
        }
        
        List<ServiceLevelCorrelatedInformation> serviceLevelCorrelatedInformation = new ArrayList<>();
        HashMap<BusinessObjectLight, List<BusinessObjectLight>> rawCorrelatedInformation = new HashMap<>();

        //Now we organize the rawServices by customers
        for (BusinessObjectLight rawService : rawServices) {
            BusinessObjectLight customer = bem.getFirstParentOfClass(rawService.getClassName(), rawService.getId(), "GenericCustomer");
            if (customer != null) {//Services without customers will be ignored. This shouldn't happen, though
                if (!rawCorrelatedInformation.containsKey(customer))
                    rawCorrelatedInformation.put(customer, new ArrayList<>());

                rawCorrelatedInformation.get(customer).add(rawService);
            }
        }

        rawCorrelatedInformation.keySet().forEach(customer -> { 
            serviceLevelCorrelatedInformation.add(new ServiceLevelCorrelatedInformation(customer, rawCorrelatedInformation.get(customer)));
        });

        return new AssetLevelCorrelatedInformation(ports, serviceLevelCorrelatedInformation);
    }
}
