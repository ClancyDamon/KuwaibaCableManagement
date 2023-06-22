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
import com.neotropic.kuwaiba.modules.commercial.sdh.api.AbstractPosition;
import com.neotropic.kuwaiba.modules.commercial.sdh.api.AvailableContainerLinkPosition;
import com.neotropic.kuwaiba.modules.commercial.sdh.api.AvailableTransportLinkPosition;
import com.neotropic.kuwaiba.modules.commercial.sdh.api.HopDefinition;
import com.neotropic.kuwaiba.modules.commercial.sdh.api.Route;
import com.neotropic.kuwaiba.modules.commercial.sdh.api.SdhContainerLinkDefinition;
import com.neotropic.kuwaiba.modules.commercial.sdh.api.SdhPosition;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLightList;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.NavigationTree;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.nodes.InventoryObjectNode;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.icons.BasicTreeNodeIconGenerator;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.wizard.Wizard;

/**
 * Wizard component to create tributary links (VC12, VC3, Unstructured VC4)
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class NewSDHTributaryLinkWizard extends Wizard {
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
    /**
     * Reference to the Business Entity Manager
     */
    private BusinessEntityManager bem;
    
    private ResourceFactory rs;
    
    private SdhService sdhService;
         

    public NewSDHTributaryLinkWizard(TranslationService ts) {
        super(ts);
    }

    public NewSDHTributaryLinkWizard(BusinessObjectLight equipmentA, BusinessObjectLight equipmentB,
            MetadataEntityManager mem, BusinessEntityManager bem, ResourceFactory rs, SdhService sdhService, TranslationService ts) {
        super(ts);
        this.equipmentA = equipmentA;
        this.equipmentB = equipmentB;
        this.mem = mem;
        this.bem = bem;
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
                transportLinkClasses = mem.getSubClassesLight("GenericSDHTributaryLink", false, false);
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
                    routes = sdhService.findSDHRoutesUsingContainerLinks(equipmentA.getClassName(),
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
                Logger.getLogger(NewSDHTributaryLinkWizard.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public Wizard.Step next() throws InvalidArgumentException {
            if (cmbRoutes.getValue() == null)
                throw new InvalidArgumentException(ts.getTranslatedString("Select a route"));
                       
            properties.put("route", cmbRoutes.getValue());
            return new ChooseTributaryLinkResourcesStep(properties);
          
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
    public class ChooseTributaryLinkResourcesStep extends Wizard.Step {

        /**
         * Own properties
         */
        private Properties properties;
        /**
         * The connection type (the class the new connection will be spawned from)
         */
        private Route route;
        
        List<HopDefinition> lstHopDefinitions ;

        
        
        public ChooseTributaryLinkResourcesStep(Properties properties) {
            
            try {
                this.properties = properties;
                this.properties.put("title", ts.getTranslatedString("module.visualization.connection-wizard-select-link-endpoints"));
                
                route = (Route) properties.get("route");
                List<BusinessObjectLightList> routes;
                String connectionType =  properties.getProperty("class");
                lstHopDefinitions = route.getLinks().stream().map(item -> new HopDefinition(item)).collect(Collectors.toList());
                Grid<HopDefinition> tblLinks = new Grid();
                tblLinks.setItems(lstHopDefinitions);
                tblLinks.addColumn(HopDefinition::getLink).setHeader("Hop");
                tblLinks.addComponentColumn(hop ->  {
                    try {                       
                        ComboBox<AbstractPosition> cbxPositions = new ComboBox<>();
                        cbxPositions.setWidthFull();
                        List<SdhContainerLinkDefinition> structure;
                        if (mem.isSubclassOf(SdhService.CLASS_GENERICSDHHIGHORDERTRIBUTARYLINK, connectionType))
                            structure = sdhService.getSDHTransportLinkStructure(hop.getLink().getClassName(), hop.getLink().getId());       
                        else
                            structure = sdhService.getSDHContainerLinkStructure(hop.getLink().getClassName(), hop.getLink().getId());
                        
                        List<AbstractPosition>  positions;
                        if (mem.isSubclassOf(SdhService.CLASS_GENERICSDHHIGHORDERTRIBUTARYLINK, connectionType))
                            positions = new ArrayList<>(Arrays.asList(buildAvailablePositionsListForTransportLinks(hop.getLink(), structure)));
                        else
                            positions = new ArrayList<>(Arrays.asList(buildAvailablePositionsListForContainers(hop.getLink(), structure)));
                      
                        cbxPositions.setItems(positions);
                        cbxPositions.addValueChangeListener(listener -> {
                          if (listener.getValue() != null) {
                             int concatenationFactor, numberOfPositionsToBeOccupied = SdhService.calculateContainerLinkCapacity(connectionType);
                             
                             String[] connectionTypeTokens = connectionType.replace("TributaryLink", "").replace("VC", "").split("-");
                             int containerType = Integer.valueOf(connectionTypeTokens[0]);
                             concatenationFactor = connectionTypeTokens.length == 1 || Integer.valueOf(connectionTypeTokens[1]) == 0 ? 
                                                    1 : Integer.valueOf(connectionTypeTokens[1]);
                            
                            switch (containerType) { //NOI18N
                                case 4: //A VC4 occupies only one position (timeslot) in a transport link
                                case 12://A VC12 occupies a single position (timeslot) in a VC4 container
                                    numberOfPositionsToBeOccupied = 1 * concatenationFactor;
                                    break;
                                case 3:
                                    numberOfPositionsToBeOccupied = 21 * concatenationFactor;
                                    break;
                                default:
                                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), "The selected connection type is not recognized as valid (VC4-XX/VC3-XX/VC12-XX)", 
                                       AbstractNotification.NotificationType.ERROR, ts).open();
                                    return;
                            }
                             
                             int selectedIndex = listener.getValue().getPosition();
                            if (positions.size() - selectedIndex < numberOfPositionsToBeOccupied) { 
                                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), "There are not enough positions to transport the concatenated container", 
                                    AbstractNotification.NotificationType.WARNING, ts).open();
                                cbxPositions.setValue(null);
                            }
                            else {
                                for (int i = selectedIndex -1; i < selectedIndex + numberOfPositionsToBeOccupied; i++) {
                                    AbstractPosition positionToBeOcuppied = positions.get(i);
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
                        Logger.getLogger(NewSDHTributaryLinkWizard.class.getName()).log(Level.SEVERE, null, ex);
                         new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                         return new Label();
                    }
                }).setHeader(ts.getTranslatedString("module.sdh.select-position"));
                
               
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
                    throw new InvalidArgumentException(ts.getTranslatedString("module.sdh.must-select-segment-positions-in-route"));
            }
            List<SdhPosition> positions = new ArrayList<>();
            for (HopDefinition hopDefinition : lstHopDefinitions) {
                positions.add(new SdhPosition(hopDefinition.getLink().getClassName(), hopDefinition.getLink().getId(), hopDefinition.getPosition()));
            }
            properties.put("positions", positions);
            return new SelectLinkEndpointsStep(properties);
          
        }   
        
        public AvailableContainerLinkPosition[] buildAvailablePositionsListForContainers(BusinessObjectLight containertLink, 
                List<SdhContainerLinkDefinition> containertLinkStructure){
            try {
                int numberOfPositions;
                
                String containerSuffix = containertLink.getClassName().replace(SdhService.CLASS_VC4, ""); //NOI18N
                if (containerSuffix.isEmpty())
                    numberOfPositions = 63;
                else
                    numberOfPositions = Math.abs(Integer.valueOf(containerSuffix)) * 63; 
                
                AvailableContainerLinkPosition[] availablePositions = new AvailableContainerLinkPosition[numberOfPositions];
                
                //First, we fill the positions we know for sure that are being used
                for (SdhContainerLinkDefinition aContainerDefinition : containertLinkStructure) {
                    int position = aContainerDefinition.getPositions().get(0).getPosition(); //This container definition has always only one position: The one used in this TransportLink
                    availablePositions[position - 1] = new AvailableContainerLinkPosition(position, aContainerDefinition.getContainer());
                    //A container might occupy more than one slot, if it's a concatenated circuit. Now, we will fill the adjacent which are also being used
                    int numberOfAdjacentPositions ;
                    String[] containerClassNameTokens = aContainerDefinition.getContainer().getClassName().split("-");
                    //Available positions are always given in terms of VC12s. A single VC3 = 21 VC12s
                    switch (containerClassNameTokens[0]) {
                        case SdhService.CLASS_VC12:
                            if (containerClassNameTokens.length == 1)
                                numberOfAdjacentPositions = 0;
                            else
                                numberOfAdjacentPositions = Integer.valueOf(containerClassNameTokens[1]) - 1;
                            break;
                        case SdhService.CLASS_VC3:
                            if (containerClassNameTokens.length == 1)
                                numberOfAdjacentPositions = 20;
                            else
                                numberOfAdjacentPositions = 21 * Integer.valueOf(containerClassNameTokens[1]) - 1;
                            break;
                        default:
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), "The total number of positions used by %s can not be determined using its class name", 
                            AbstractNotification.NotificationType.ERROR, ts).open();                        
                            return new AvailableContainerLinkPosition[0];
                    }
                        
                    for (int j = position; j < position + numberOfAdjacentPositions; j++)
                        availablePositions[j] = new AvailableContainerLinkPosition(j + 1, aContainerDefinition.getContainer());                        
                }
                
                //Then we fill the rest (if any) with free slots
                for (int i = 1; i <= numberOfPositions; i++) {
                    if (availablePositions[i - 1] == null)
                        availablePositions[i - 1] = new AvailableContainerLinkPosition(i, null);
                }
                return availablePositions;
            } catch (NumberFormatException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), "A malformed container link class name was found. Please make sure that you are using the right naming format", 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                return new AvailableContainerLinkPosition[0];
            }
        }
        
        public AvailableTransportLinkPosition[] buildAvailablePositionsListForTransportLinks(BusinessObjectLight transportLink, 
                List<SdhContainerLinkDefinition> transportLinkStructure) {
            try {
                int numberOfVC4 = SdhService.calculateTransportLinkCapacity(transportLink.getClassName());
                if (numberOfVC4 < 0) {
                     new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), "The TransportLink class name does not allow to calculate the total number of positions", 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                     return new AvailableTransportLinkPosition[0];
                }
                    
                AvailableTransportLinkPosition[] availablePositions = new AvailableTransportLinkPosition[numberOfVC4];
                
                //First, we fill the positions we know for sure that are being used
                for (SdhContainerLinkDefinition aContainerDefinition : transportLinkStructure) {
                    int position = aContainerDefinition.getPositions().get(0).getPosition(); //This container definition has always only one position: The one used in this TransportLink
                    availablePositions[position - 1] = new AvailableTransportLinkPosition(position, aContainerDefinition.getContainer());
                    //A container might occupy more than one slot, if it's a concatenated circuit. Now, we will fill the adjacent which are also being used
                   
                    int numberOfAdjacentPositions = SdhService.calculateContainerLinkCapacity(aContainerDefinition.getContainer().getClassName());
                    if (numberOfAdjacentPositions < 0) {                        
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), "The ContainerLink class name does not allow to calculate the total number of concatenated positions", 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                        return new AvailableTransportLinkPosition[0];
                    }

                    numberOfAdjacentPositions -= 1;  //Minus one, because we've already filled the first position                                                                                                     //Absolute value, because the concatenated containers class names are like "VC4-A_NUMBER"
                    for (int j = position; j < position + numberOfAdjacentPositions; j++)
                        availablePositions[j] = new AvailableTransportLinkPosition(j + 1, aContainerDefinition.getContainer());
        
                }
                
                //Then we fill the rest (if any) with free slots
                for (int i = 1; i <= numberOfVC4; i++) {
                    if (availablePositions[i - 1] == null)
                        availablePositions[i - 1] = new AvailableTransportLinkPosition(i, null);
                }
                return availablePositions;
            } catch (NumberFormatException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), "The TransportLink class name does not allow to calculate the total number of positions", 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                return new AvailableTransportLinkPosition[0];
            }
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
    public class SelectLinkEndpointsStep extends Step {
        /**
         * The tree on the left side of the wizard
         */
        private TreeGrid<InventoryObjectNode> aSideTree;
        /**
         * The tree on the right side of the wizard
         */
        private TreeGrid<InventoryObjectNode> bSideTree;
        /**
         * Own properties
         */
        private Properties properties;
        
        private BusinessObjectLight selectedEndPointA;
        private BusinessObjectLight selectedEndPointB;
        
        
        public SelectLinkEndpointsStep(Properties properties) {
            
            this.properties = properties;
            this.properties.put("title", ts.getTranslatedString("module.visualization.connection-wizard-select-link-endpoints"));
                    
            HierarchicalDataProvider dataProviderSourceTree = buildTreeHierarchicalDataProvider(equipmentA);
            HierarchicalDataProvider dataProviderTargetTree = buildTreeHierarchicalDataProvider(equipmentB);
            aSideTree = new NavigationTree(dataProviderSourceTree , new BasicTreeNodeIconGenerator(rs));
            bSideTree = new NavigationTree(dataProviderTargetTree , new BasicTreeNodeIconGenerator(rs));        
            aSideTree.setMaxHeight("360px");
            bSideTree.setMaxHeight("360px");
             
            Label lblSelectedEndPointsTitle = new BoldLabel(String.format(ts.getTranslatedString("module.visualization.connection-wizard-selected-endpoints"), "", ""));
            Label lblSelectedEndPoints = new Label();
            HorizontalLayout lytSelectedEndPoints = new HorizontalLayout(lblSelectedEndPointsTitle, lblSelectedEndPoints);
            lytSelectedEndPoints.setPadding(false);
            lytSelectedEndPoints.getElement().getStyle().set("z-index", "1000");
            aSideTree.addItemClickListener( item -> {
                selectedEndPointA = item.getItem().getObject().equals(selectedEndPointA) ? null : item.getItem().getObject();
                lblSelectedEndPoints.setText(String.format(" %s - %s",
                        selectedEndPointA == null ? "" : selectedEndPointA.getName(),
                        selectedEndPointB == null ? "" : selectedEndPointB.getName()));
            });
            bSideTree.addItemClickListener( item -> {
                selectedEndPointB = item.getItem().getObject().equals(selectedEndPointB) ? null : item.getItem().getObject();
                lblSelectedEndPoints.setText(String.format(" %s - %s",
                        selectedEndPointA == null ? "" : selectedEndPointA.getName(),
                        selectedEndPointB == null ? "" : selectedEndPointB.getName()));
            });
            
            HorizontalLayout lytTrees = new HorizontalLayout(aSideTree, bSideTree);
            lytTrees.setMaxHeight("360px");
            lytTrees.setWidthFull();
            lytTrees.setMargin(false);
            lytTrees.setSpacing(true);
            this.add(lytTrees, lytSelectedEndPoints);
            this.setSpacing(true);
            this.setWidthFull();
        }

        @Override
        public Step next() throws InvalidArgumentException {
            if (aSideTree.getSelectedItems().isEmpty() || bSideTree.getSelectedItems().isEmpty())
                throw new InvalidArgumentException(ts.getTranslatedString("module.visualization.connection-wizard-select-both-endpoints"));

            try {
                if (!mem.isSubclassOf(Constants.CLASS_GENERICPORT, selectedEndPointA.getClassName()) || !mem.isSubclassOf(Constants.CLASS_GENERICPORT, selectedEndPointB.getClassName()))
                    throw new InvalidArgumentException(ts.getTranslatedString("module.visualization.connection-wizard-only-ports-can-be-connected-using-links"));
                else {
                    properties.put("aSidePort", selectedEndPointA);
                    properties.put("bSidePort", selectedEndPointB);

                    return new SelectServiceStep(properties);
                }
            } catch (MetadataObjectNotFoundException ex) {
                throw new InvalidArgumentException(ex.getLocalizedMessage());
            }
            
        }
        
        /**
     * Function that creates a new HierarchicalDataProvider for a tree grid.
     * @param root the main toot of the tree
     * @return the new data Provider the the given root
     */
    public HierarchicalDataProvider buildTreeHierarchicalDataProvider(BusinessObjectLight root) {
        return new AbstractBackEndHierarchicalDataProvider<InventoryObjectNode, Void>() {
            @Override
            protected Stream<InventoryObjectNode> fetchChildrenFromBackEnd(HierarchicalQuery<InventoryObjectNode, Void> query) {
                InventoryObjectNode parent = query.getParent();
                if (parent != null) {
                    if (parent.getObject().getClassName() == null || parent.getObject().getClassName().isEmpty()) 
                        return new ArrayList().stream();
                    BusinessObjectLight object = parent.getObject();
                    try {
                        List<BusinessObjectLight> children = bem.getObjectChildren(object.getClassName(), object.getId(), -1);
                        List<InventoryObjectNode> theChildren = new ArrayList();
                        for (BusinessObjectLight child : children)
                            theChildren.add(new InventoryObjectNode(child));
                        return theChildren.stream();
                    } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
                        return new ArrayList().stream();
                    } 
                } else {   
                       
                        return Arrays.asList(new InventoryObjectNode(root)).stream();                   
                }              
            }

            @Override
            public int getChildCount(HierarchicalQuery<InventoryObjectNode, Void> query) {
                InventoryObjectNode parent = query.getParent();
                if (parent != null) {
                    try {
                        BusinessObjectLight object = parent.getObject();
                        return (int) bem.getObjectChildrenCount(object.getClassName(), object.getId(), null);
                    } catch (InvalidArgumentException ex) {
                        Logger.getLogger(SelectLinkEndpointsStep.class.getName()).log(Level.SEVERE, null, ex);
                        return 0;
                    }
                    
                } else
                    return 1;
            }

            @Override
            public boolean hasChildren(InventoryObjectNode node) {
                return true;
            }
        };
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
    public class SelectServiceStep extends Step {
        /**
         * Own properties
         */
        private Properties properties;
        
        private BusinessObjectLight selectedService;
                
        public SelectServiceStep(Properties properties) {
            
            this.properties = properties;
            this.properties.put("title", ts.getTranslatedString("module.visualization.connection-wizard-select-link-endpoints"));
                    
            try {
                Label lblTitle = new Label(ts.getTranslatedString("module.sdh.select-service"));
                ComboBox<BusinessObjectLight> cbxServices = new ComboBox<>();        
                cbxServices.setWidth("300px");
                List<BusinessObjectLight> services = bem.getObjectsOfClassLight(SdhService.CLASS_GENERICSDHSERVICE, new HashMap<String, String>(), -1, -1);
                cbxServices.setItems(services);
                cbxServices.addValueChangeListener(listener -> {
                   selectedService = listener.getValue();
                });
                this.add(lblTitle, cbxServices);
                this.setSpacing(true);
                this.setWidthFull();
            } catch (InvalidArgumentException | MetadataObjectNotFoundException ex) {
                Logger.getLogger(NewSDHTributaryLinkWizard.class.getName()).log(Level.SEVERE, null, ex);
            }
     
        }

        @Override
        public Step next() throws InvalidArgumentException {
                       
            try {
                    String connectionType = properties.getProperty("class");
                    String connectionName = properties.getProperty("name");
                    List<SdhPosition> positions = (List<SdhPosition>) properties.get("positions");
                    BusinessObjectLight sourcePort = (BusinessObjectLight) properties.get("aSidePort");
                    BusinessObjectLight targetPort = (BusinessObjectLight) properties.get("bSidePort");
                    
                    String newTributaryLinkId = sdhService.createSDHTributaryLink(sourcePort.getClassName(), sourcePort.getId(), targetPort.getClassName(), 
                                                                                           targetPort.getId(), connectionType, positions, connectionName);    
                    BusinessObjectLight service = (BusinessObjectLight) properties.get("service");
                    if (service != null) {
                         bem.createSpecialRelationship(service.getClassName(), service.getId(),
                                connectionType, newTributaryLinkId, "uses", true); //NOI18N   
                         properties.put("serviceRelated", true);
                    } else
                        properties.put("serviceRelated", false);
          
                    properties.put("connection", new BusinessObjectLight(properties.getProperty("class"), newTributaryLinkId, properties.getProperty("name")));
                    
                    return null;
                
            } catch (IllegalStateException | InvalidArgumentException | MetadataObjectNotFoundException | OperationNotPermittedException ex) {
                throw new InvalidArgumentException(ex.getLocalizedMessage());
            } catch (InventoryException ex) {
                throw new InvalidArgumentException(ex.getLocalizedMessage());
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
