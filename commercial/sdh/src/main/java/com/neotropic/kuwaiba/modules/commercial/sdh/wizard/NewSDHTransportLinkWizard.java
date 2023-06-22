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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
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
import org.neotropic.util.visual.icons.BasicTreeNodeIconGenerator;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.wizard.Wizard;

/**
 * Wizard component to create transport links (STMX)
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class NewSDHTransportLinkWizard extends Wizard {
    
    /**
     * source equipment
     */
    BusinessObjectLight equipmentA;
    /**
     * target equipment
     */
    BusinessObjectLight equipmentB;

    /**
     * Reference to the Business Entity Manager
     */
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager
     */
    private MetadataEntityManager mem;
    
    private ResourceFactory rs;
    
    private SdhService sdhService;
         

    public NewSDHTransportLinkWizard(TranslationService ts) {
        super(ts);
    }

    public NewSDHTransportLinkWizard(BusinessObjectLight equipmentA, BusinessObjectLight equipmentB, BusinessEntityManager bem, MetadataEntityManager mem, ResourceFactory rs, SdhService sdhService, TranslationService ts) {
        super(ts);
        this.equipmentA = equipmentA;
        this.equipmentB = equipmentB;
        this.mem = mem;
        this.bem = bem;
        this.rs = rs;
        this.sdhService = sdhService;
        build(new GeneralInfoStep());
    }

    public class GeneralInfoStep extends Step {
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
            
            txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setClassName("width300px");            
            List<ClassMetadataLight> transportLinkClasses = new ArrayList<>();
            try {
                transportLinkClasses = mem.getSubClassesLight("GenericSDHTransportLink", false, false);
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
        public Step next() throws InvalidArgumentException  {
            if (txtName.getValue().trim().isEmpty()  || cmbConnectionClass.getValue() == null)
                throw new InvalidArgumentException(ts.getTranslatedString("module.visualization.connection-wizard-fill-fields"));
            properties.put("name", txtName.getValue());
            properties.put("class", cmbConnectionClass.getValue().getName());
            properties.put("equipmentA", equipmentA);
            properties.put("equipmentB", equipmentB);
                        
            return new SelectLinkEndpointsStep(properties);
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
            
            aSideTree.addItemClickListener( item -> {
                selectedEndPointA = item.getItem().getObject();
            });
            bSideTree.addItemClickListener( item -> {
                selectedEndPointB = item.getItem().getObject();
            });
            
            HorizontalLayout lytTrees = new HorizontalLayout(aSideTree, bSideTree);
            lytTrees.setMaxHeight("360px");
            lytTrees.setWidthFull();
            lytTrees.setMargin(false);
            lytTrees.setSpacing(true);
            this.add(lytTrees);
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
                    properties.put("aSide", selectedEndPointA);
                    properties.put("bSide", selectedEndPointB);
                    properties.put("equipmentA", equipmentA);
                    properties.put("equipmentB", equipmentB);
                    
                    String newConnection = sdhService.createSDHTransportLink(selectedEndPointA.getClassName(), selectedEndPointA.getId(), selectedEndPointB.getClassName(), 
                            selectedEndPointB.getId(), properties.getProperty("class"), properties.getProperty("name"));
                    
                    properties.put("connection", new BusinessObjectLight(properties.getProperty("class"), newConnection, properties.getProperty("name")));
                    
                    return null;
                }
            } catch (IllegalStateException | InvalidArgumentException | MetadataObjectNotFoundException | OperationNotPermittedException ex) {
                throw new InvalidArgumentException(ex.getLocalizedMessage());
            } catch (InventoryException ex) {
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
            return true;
        }
        
        @Override
        public Properties getProperties() {
            return properties;
        }
    }
    
    

}
