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

package com.neotropic.kuwaiba.modules.commercial.sdh.wizard;

import com.neotropic.kuwaiba.modules.commercial.sdh.SdhService;
import com.neotropic.kuwaiba.modules.commercial.sdh.api.AvailableTransportLinkPosition;
import com.neotropic.kuwaiba.modules.commercial.sdh.api.HopDefinition;
import com.neotropic.kuwaiba.modules.commercial.sdh.api.Route;
import com.neotropic.kuwaiba.modules.commercial.sdh.api.SdhContainerLinkDefinition;
import com.neotropic.kuwaiba.modules.commercial.sdh.api.SdhPosition;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLightList;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.wizard.Wizard;

/**
 * Wizard component to create transport links (Structured VC4)
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class NewSDHContainerLinkWizard extends Wizard {
    
    /**
     * source equipment
     */
    BusinessObjectLight equipmentA;
    /**
     * target equipment
     */
    BusinessObjectLight equipmentB;

    /**
     * Reference to the Metadata Entity Manager
     */
    private MetadataEntityManager mem;
    
    private ResourceFactory rs;
    
    private SdhService sdhService;
         

    public NewSDHContainerLinkWizard(TranslationService ts) {
        super(ts);
    }

    public NewSDHContainerLinkWizard(BusinessObjectLight equipmentA, BusinessObjectLight equipmentB, MetadataEntityManager mem, ResourceFactory rs, SdhService sdhService, TranslationService ts) {
        super(ts);
        this.equipmentA = equipmentA;
        this.equipmentB = equipmentB;
        this.mem = mem;
        this.rs = rs;
        this.sdhService = sdhService;
        build(new GeneralInfoStep());
    }

    public class GeneralInfoStep extends Wizard.Step {
        /**
         * The name of the new connection
         */
        private TextField txtName;
        /**
         * The connection type (the class the new connection will be spawned from)
         */
        private ComboBox<ClassMetadataLight> cmbConnectionClass;
        /**
         * Own properties
         */
        private Properties properties;     
        
    public GeneralInfoStep() {

            properties = new Properties();
            properties.put("title", ts.getTranslatedString("module.visualization.connection-wizard.general-info"));
            properties.put("rootASide", equipmentA);
            properties.put("rootBSide", equipmentB);
            
            txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setClassName("width300px");            
            List<ClassMetadataLight> transportLinkClasses = new ArrayList<>();
            try {
                transportLinkClasses = mem.getSubClassesLight("GenericSDHHighOrderContainerLink", false, false);
            } catch (MetadataObjectNotFoundException ex) {
                Logger.getLogger(NewSDHTransportLinkWizard.class.getName()).log(Level.SEVERE, null, ex);
            }
            cmbConnectionClass = new ComboBox<>(ts.getTranslatedString("module.visualization.connection-wizard-connection-class"));
            cmbConnectionClass.setAllowCustomValue(false);
            cmbConnectionClass.setItems(transportLinkClasses);
            cmbConnectionClass.setRequiredIndicatorVisible(true);
            cmbConnectionClass.setLabel(ts.getTranslatedString("module.visualization.connection-wizard-select-connection-class"));
            cmbConnectionClass.setClassName("width300px");
                             
            add(txtName, cmbConnectionClass);
            setSizeFull();
        }

        @Override
        public Wizard.Step next() throws InvalidArgumentException  {
            if (txtName.getValue().trim().isEmpty()  || cmbConnectionClass.getValue() == null)
                throw new InvalidArgumentException(ts.getTranslatedString("module.visualization.connection-wizard-fill-fields"));
            if (SdhService.calculateContainerLinkCapacity(cmbConnectionClass.getValue().getName()) < 0) 
                throw new InvalidArgumentException(ts.getTranslatedString("The ContainerLink class name does not allow to calculate the total number of concatenated positions"));
            properties.put("name", txtName.getValue());
            properties.put("class", cmbConnectionClass.getValue().getName());
                        
            return new ChooseRouteStep(properties);
        }

        @Override
        public boolean isFinal() {
            return false;
        }

        @Override
        public Properties getProperties() {
            return properties;
        }
        
        public class ConnectionType {
            private int type;
            private String displayName;

            public ConnectionType(int type, String displayName) {
                this.type = type;
                this.displayName = displayName;
            }

            public int getType() {
                return type;
            }
            
            @Override
            public String toString() {
                return displayName;
            }
        }
    }
    
    /**
     * Step to select the endpoints
     */
    public class ChooseRouteStep extends Wizard.Step {

        /**
         * Own properties
         */
        private Properties properties;
        /**
         * The connection type (the class the new connection will be spawned from)
         */
        private ComboBox<Route> cmbRoutes;

        
        
        public ChooseRouteStep(Properties properties) {
            
            try {
                this.properties = properties;
                this.properties.put("title", ts.getTranslatedString("module.visualization.connection-wizard-select-link-endpoints"));
                
                List<BusinessObjectLightList> routes;
                String connectionType = properties.getProperty("class");
                if (mem.isSubclassOf(SdhService.CLASS_GENERICSDHHIGHORDERCONTAINERLINK, connectionType) ||
                        mem.isSubclassOf(SdhService.CLASS_GENERICSDHHIGHORDERTRIBUTARYLINK, connectionType))
                    routes = sdhService.findSDHRoutesUsingTransportLinks(equipmentA.getClassName(),
                            equipmentA.getId(), equipmentB.getClassName(), equipmentB.getId());
                else
                    routes = sdhService.findSDHRoutesUsingTransportLinks(equipmentA.getClassName(),
                            equipmentA.getId(), equipmentB.getClassName(), equipmentB.getId());
                
                Grid<BusinessObjectLight> lstHops = new Grid();
                lstHops.addColumn(BusinessObjectLight::toString);
                
                List<Route> lstRoutes = new ArrayList<>();
                for (int i = 0; i < routes.size(); i++) 
                    lstRoutes.add(new Route(String.format("Route %s", i+1), routes.get(i).getList(), mem));
                
                cmbRoutes = new ComboBox<>(ts.getTranslatedString("Routes"));
                cmbRoutes.setAllowCustomValue(false);
                cmbRoutes.setItems(lstRoutes);
                 if (!lstRoutes.isEmpty()) {
                    cmbRoutes.setValue(lstRoutes.get(0));
                    lstHops.setItems(lstRoutes.get(0).getHops());
                }
                cmbRoutes.setRequiredIndicatorVisible(true);
                cmbRoutes.setClassName("width300px");
                cmbRoutes.addValueChangeListener(listener -> {
                   if (listener.getValue() != null) 
                       lstHops.setItems(listener.getValue().getHops());
                    else
                       lstHops.setItems(new ArrayList<>());
                    lstHops.getDataProvider().refreshAll();    
                });
                        
                
               
                this.add(cmbRoutes, lstHops);
                this.setSpacing(true);
                this.setWidthFull();
            } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                Logger.getLogger(NewSDHContainerLinkWizard.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public Wizard.Step next() throws InvalidArgumentException {
            if (cmbRoutes.getValue() == null)
                throw new InvalidArgumentException(ts.getTranslatedString("Select a route"));
                       
            properties.put("route", cmbRoutes.getValue());
            return new ChooseContainerLinkResourcesStep(properties);
          
        }        
    
        @Override
        public boolean isFinal() {
            return false;
        }
        
        @Override
        public Properties getProperties() {
            return properties;
        }
    }
    
    
    /**
     * Step to select the endpoints
     */
    public class ChooseContainerLinkResourcesStep extends Wizard.Step {

        /**
         * Own properties
         */
        private Properties properties;
        /**
         * The connection type (the class the new connection will be spawned from)
         */
        private Route route;
        
        List<HopDefinition> lstHopDefinitions ;

        
        
        public ChooseContainerLinkResourcesStep(Properties properties) {
            
            try {
                this.properties = properties;
                this.properties.put("title", ts.getTranslatedString("module.visualization.connection-wizard-select-link-endpoints"));
                
                route = (Route) properties.get("route");
                List<BusinessObjectLightList> routes;
                String connectionType =  properties.getProperty("class");
                lstHopDefinitions = route.getLinks().stream().map(item -> new HopDefinition(item)).collect(Collectors.toList());
                Grid<HopDefinition> tblLinks = new Grid();
                tblLinks.setItems(lstHopDefinitions);
                tblLinks.addColumn(HopDefinition::getLink).setHeader("module.sdh.select-position");
                tblLinks.addComponentColumn(hop ->  {
                    try {                       
                        ComboBox<AvailableTransportLinkPosition> cbxPositions = new ComboBox<>();
                        cbxPositions.setWidthFull();
                        List<SdhContainerLinkDefinition> linkDefinition = sdhService.getSDHTransportLinkStructure(hop.getLink().getClassName(), hop.getLink().getId());
                        List<AvailableTransportLinkPosition>  positions = new ArrayList<>(Arrays.asList(buildAvailablePositionsList(hop.getLink(), linkDefinition)));
                        cbxPositions.setItems(positions);
                        cbxPositions.addValueChangeListener(listener -> {
                          if (listener.getValue() != null) {
                             int numberOfPositionsToBeOccupied = SdhService.calculateContainerLinkCapacity(connectionType);
                             int selectedIndex = listener.getValue().getPosition();
                            if (positions.size() - selectedIndex < numberOfPositionsToBeOccupied) { 
                                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), "There are not enough positions to transport the concatenated container", 
                                    AbstractNotification.NotificationType.WARNING, ts).open();
                                cbxPositions.setValue(null);
                            }
                            else {
                                for (int i = selectedIndex -1; i < selectedIndex + numberOfPositionsToBeOccupied; i++) {
                                    AvailableTransportLinkPosition positionToBeOcuppied = positions.get(i);
                                    if (positionToBeOcuppied.getContainer() != null) {
                                        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), "One of the positions to be assigned is already in use", 
                                           AbstractNotification.NotificationType.WARNING, ts).open();        
                                        cbxPositions.setValue(null);
                                        return;
                                    }   
                                }
                                hop.setPosition(selectedIndex);
                            }
                           } else {
                              hop.setPosition(-1);
                          }
                        });
                        return cbxPositions;
                    } catch (InvalidArgumentException | BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
                        Logger.getLogger(NewSDHContainerLinkWizard.class.getName()).log(Level.SEVERE, null, ex);
                         new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                         return new Label();
                    }
                }).setHeader("Hop");
                
               
                this.add(tblLinks);
                this.setSpacing(true);
                this.setWidthFull();
            } catch (MetadataObjectNotFoundException ex) {
                 new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }

        @Override
        public Wizard.Step next() throws InvalidArgumentException {

            for (HopDefinition lstHopDefinition : lstHopDefinitions) {
                if (lstHopDefinition.getPosition() == -1)
                    throw new InvalidArgumentException("You have to select a position for every segment of the route");
            }
            List<SdhPosition> positions = new ArrayList<>();
            for (HopDefinition hopDefinition : lstHopDefinitions) {
                positions.add(new SdhPosition(hopDefinition.getLink().getClassName(), hopDefinition.getLink().getId(), hopDefinition.getPosition()));
            }
            String linkId = sdhService.createSDHContainerLink(equipmentA.getClassName(),
                            equipmentA.getId(), equipmentB.getClassName(), equipmentB.getId(), properties.getProperty("class"), positions, properties.getProperty("name"));
            properties.put("connection", new BusinessObjectLight(properties.getProperty("class"), linkId, properties.getProperty("name")));
            return null;
          
        }  
        
        public AvailableTransportLinkPosition[] buildAvailablePositionsList(BusinessObjectLight transportLink, 
                List<SdhContainerLinkDefinition> transportLinkStructure) {
            try {
                int numberOfVC4 = SdhService.calculateTransportLinkCapacity(transportLink.getClassName());
                AvailableTransportLinkPosition[] availablePositions = new AvailableTransportLinkPosition[numberOfVC4];
                
                //First, we fill the positions we know for sure that are being used
                for (SdhContainerLinkDefinition aContainerDefinition : transportLinkStructure) {
                    int position = aContainerDefinition.getPositions().get(0).getPosition(); //This container definition has always only one position: The one used in this TransportLink
                    availablePositions[position - 1] = new AvailableTransportLinkPosition(position, aContainerDefinition.getContainer());
                    //A container might occupy more than one slot, if it's a concatenated circuit. Now, we will fill the adjacent which are also being used
                    try {
                        int numberOfAdjacentPositions = 0;
                        String adjacentPositions = aContainerDefinition.getContainer().getClassName().replace("VC4", "");
                        if (!adjacentPositions.isEmpty())
                            numberOfAdjacentPositions = Math.abs(Integer.valueOf(adjacentPositions)) - 1; //Minus one, because we've already filled the first position
                                                                                                          //Absolute value, because the concatenated containers class names are like "VC4-A_NUMBER"
                        for (int j = position; j < position + numberOfAdjacentPositions; j++)
                            availablePositions[j] = new AvailableTransportLinkPosition(j + 1, aContainerDefinition.getContainer());
                        
                    } catch (NumberFormatException ex) {
                        return new AvailableTransportLinkPosition[0];
                    }
                }
                
                //Then we fill the rest (if any) with free slots
                for (int i = 1; i <= numberOfVC4; i++) {
                    if (availablePositions[i - 1] == null)
                        availablePositions[i - 1] = new AvailableTransportLinkPosition(i, null);
                }
                return availablePositions;
            } catch (NumberFormatException ex) {
                return new AvailableTransportLinkPosition[0];
            }
        }
    
        @Override
        public boolean isFinal() {
            return true;
        }
        
        @Override
        public Properties getProperties() {
            return properties;
        }
    }
    
}
