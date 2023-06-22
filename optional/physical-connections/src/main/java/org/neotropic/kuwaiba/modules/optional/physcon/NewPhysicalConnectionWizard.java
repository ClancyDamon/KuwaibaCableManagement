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

package org.neotropic.kuwaiba.modules.optional.physcon;


import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
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
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
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
 * A wizard that given two initial objects, guides the user through the creation of a physical connection (link or container)
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class NewPhysicalConnectionWizard extends Wizard {
    
    private PhysicalConnectionsService physicalConnectionsService;
    private BusinessEntityManager bem;
    private ApplicationEntityManager aem;
    private MetadataEntityManager mem;
    private ResourceFactory rs;
    
    public NewPhysicalConnectionWizard(BusinessObjectLight rootASide, BusinessObjectLight rootBSide, BusinessEntityManager bem, ApplicationEntityManager aem, MetadataEntityManager mem,
                PhysicalConnectionsService physicalConnectionsService, ResourceFactory rs, TranslationService ts) {
        super(ts);
        build(new GeneralInfoStep(rootASide, rootBSide, bem, aem, mem, physicalConnectionsService, rs, ts));
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.rs = rs;
        this.ts = ts;
        this.physicalConnectionsService = physicalConnectionsService;
       
    }
    
    /**
     * The user must choose if he/she wants to create a link or a container and what template (if any) 
     * should be used and provide general information like the name of the new connection and what class 
     * and template should be used for the new object
     */
    public class GeneralInfoStep extends Step {
        /**
         * The name of the new connection
         */
        private TextField txtName;
        /**
         * If the connection is a container or a link
         */
        private ComboBox<ConnectionType> cmbConnectionType;
        /**
         * The connection type (the class the new connection will be spawned from)
         */
        private ComboBox<ClassMetadataLight> cmbConnectionClass;
        /**
         * The list of available templates
         */
        private ComboBox<TemplateObjectLight> cmbTemplates;
        /**
         * Should the connection be created from a template
         */
        private Checkbox chkHasTemplate;
        /**
         * Own properties
         */
        private Properties properties;
        
        /**
     * Reference to the Application Entity Manager
     */
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager
     */
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager
     */
    private MetadataEntityManager mem;
    
    private ResourceFactory rs;
    
    private TranslationService ts;
    
    private PhysicalConnectionsService physicalConnectionsService;
        
    public GeneralInfoStep(BusinessObjectLight rootASide, BusinessObjectLight rootBSide, BusinessEntityManager bem, 
            ApplicationEntityManager aem, MetadataEntityManager mem, PhysicalConnectionsService physicalConnectionsService,ResourceFactory rs, TranslationService ts) {
            
            this.aem = aem;
            this.bem = bem;
            this.mem = mem;
            this.rs = rs;
            this.ts = ts;
            this.physicalConnectionsService = physicalConnectionsService;
            properties = new Properties();
            properties.put("title", ts.getTranslatedString("module.visualization.connection-wizard.general-info"));
            properties.put("rootASide", rootASide);
            properties.put("rootBSide", rootBSide);
            
            txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setClassName("width300px");
            
            cmbConnectionType = new ComboBox<>(ts.getTranslatedString("module.visualization.connection-wizard-connection-type"), Arrays.asList(new ConnectionType(1, "Connect Using a Container"), 
                    new ConnectionType(2, ts.getTranslatedString("module.visualization.connection-wizard-connect-using-link"))));
            cmbConnectionType.setAllowCustomValue(false);          
            cmbConnectionType.setRequiredIndicatorVisible(true);
            cmbConnectionType.setLabel(ts.getTranslatedString("module.visualization.connection-wizard-select-connection-type"));
            cmbConnectionType.setClassName("width300px");
            
            cmbConnectionType.addValueChangeListener((newSelection) -> {
                try {
                    if (newSelection.getValue() != null) {
                        if (newSelection.getValue().getType() == 1)
                            cmbConnectionClass.setItems(this.mem.getSubClassesLight(Constants.CLASS_GENERICPHYSICALCONTAINER, false, false));
                        else
                            cmbConnectionClass.setItems(this.mem.getSubClassesLight(Constants.CLASS_GENERICPHYSICALLINK, false, false));
                    }
                } catch (MetadataObjectNotFoundException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
                }
            });
            
            cmbConnectionClass = new ComboBox<>(ts.getTranslatedString("module.visualization.connection-wizard-connection-class"));
            cmbConnectionClass.setAllowCustomValue(false);
            cmbConnectionClass.setRequiredIndicatorVisible(true);
            cmbConnectionClass.setLabel(ts.getTranslatedString("module.visualization.connection-wizard-select-connection-class"));
            cmbConnectionClass.setClassName("width300px");
            cmbConnectionClass.addValueChangeListener((newSelection) -> {
                try {
                    if (newSelection.getValue() != null) {
                        cmbTemplates.setItems(this.aem.getTemplatesForClass(newSelection.getValue().getName()));
                    }
                } catch (MetadataObjectNotFoundException ex) {
                    Logger.getLogger(NewPhysicalConnectionWizard.class.getName()).log(Level.SEVERE, null, ex);
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
                }
            });
            
            cmbTemplates = new ComboBox<>(ts.getTranslatedString("module.visualization.connection-wizard-template"));
            cmbTemplates.setEnabled(false);
            cmbTemplates.setClassName("width300px");
            chkHasTemplate = new Checkbox(ts.getTranslatedString("module.visualization.connection-wizard-use-template"));
            chkHasTemplate.addValueChangeListener((newSelection) -> {
                cmbTemplates.setEnabled(chkHasTemplate.getValue());
            });
            HorizontalLayout lytTemplate = new HorizontalLayout(chkHasTemplate, cmbTemplates);
            lytTemplate.setAlignItems(Alignment.BASELINE);
            add(txtName, cmbConnectionType, cmbConnectionClass, lytTemplate);
            setSizeFull();
        }

        @Override
        public Step next() throws InvalidArgumentException  {
            if (txtName.getValue().trim().isEmpty() || cmbConnectionType.getValue() == null
                    || cmbConnectionClass.getValue() == null || (chkHasTemplate.getValue() && cmbTemplates.getValue() == null))
                throw new InvalidArgumentException(ts.getTranslatedString("module.visualization.connection-wizard-fill-fields"));
            properties.put("name", txtName.getValue());
            properties.put("class", cmbConnectionClass.getValue().getName());
            
            properties.put("templateId", chkHasTemplate.getValue() ? cmbTemplates.getValue().getId() : "");
            
            if (cmbConnectionType.getValue().type == 1)
                return new SelectContainerEndpointsStep(properties, bem ,aem , mem , physicalConnectionsService, rs);
            else
                return new SelectLinkEndpointsStep(properties, bem ,aem , mem , physicalConnectionsService, rs);
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
     * Step to select the endpoints if the connection type selected in the past step was a container
     */
    public class SelectContainerEndpointsStep extends Step {
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
        
        private ResourceFactory rs;
        
        private BusinessEntityManager bem;
        private ApplicationEntityManager aem;
        private MetadataEntityManager mem;
        
        private BusinessObjectLight selectedEndPointA;
        private BusinessObjectLight selectedEndPointB;
        
        private PhysicalConnectionsService physicalConnectionsService;
        
        public SelectContainerEndpointsStep(Properties properties, BusinessEntityManager bem, ApplicationEntityManager aem, MetadataEntityManager mem,
                PhysicalConnectionsService physicalConnectionsService, ResourceFactory rs) {
            
            this.aem = aem;
            this.bem = bem;
            this.mem = mem;
            this.physicalConnectionsService = physicalConnectionsService;
            this.rs = rs;
            this.properties = properties;
            this.properties.put("title", ts.getTranslatedString("module.visualization.connection-wizard-select-container-endpoints"));
                       
            HierarchicalDataProvider dataProviderSourceTree = buildTreeHierarchicalDataProvider((BusinessObjectLight)properties.get("rootASide"));
            HierarchicalDataProvider dataProviderTargetTree = buildTreeHierarchicalDataProvider((BusinessObjectLight)properties.get("rootBSide"));
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
                if (mem.isSubclassOf(Constants.CLASS_GENERICPORT, selectedEndPointA.getClassName()) 
                               || mem.isSubclassOf(Constants.CLASS_GENERICPORT , selectedEndPointB.getClassName()))
                    throw new InvalidArgumentException(ts.getTranslatedString("module.visualization.connection-wizard-ports-cant-be-enpoints-containers"));
                else {
                    properties.put("aSide", selectedEndPointA);
                    properties.put("bSide", selectedEndPointB);
                    Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                    String newConnection = physicalConnectionsService.createPhysicalConnection(selectedEndPointA.getClassName(), selectedEndPointA.getId(), selectedEndPointB.getClassName(), 
                            selectedEndPointB.getId(), properties.getProperty("name"), properties.getProperty("class"), 
                            (String)properties.get("templateId"), session.getUser().getUserName());
                    
                    properties.put("connection", new BusinessObjectLight(properties.getProperty("class"), newConnection, properties.getProperty("name")));
                    
                    return null;
                }
            }          
          catch (IllegalStateException | OperationNotPermittedException | MetadataObjectNotFoundException ex) {
                Logger.getLogger(NewPhysicalConnectionWizard.class.getName()).log(Level.SEVERE, null, ex);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
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
                        Logger.getLogger(SelectContainerEndpointsStep.class.getName()).log(Level.SEVERE, null, ex);
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
    
    /**
     * Step to select the endpoints if the connection type selected in the past step was a link
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
        
        private BusinessEntityManager bem;
        private ApplicationEntityManager aem;
        private MetadataEntityManager mem;
        private ResourceFactory rs;
        private BusinessObjectLight selectedEndPointA;
        private BusinessObjectLight selectedEndPointB;
        
        private PhysicalConnectionsService physicalConnectionsService;
        
        public SelectLinkEndpointsStep(Properties properties, BusinessEntityManager bem, ApplicationEntityManager aem, MetadataEntityManager mem,
                PhysicalConnectionsService physicalConnectionsService, ResourceFactory rs) {
            
            this.aem = aem;
            this.bem = bem;
            this.mem = mem;
            this.physicalConnectionsService = physicalConnectionsService;
            this.rs = rs;
            this.properties = properties;
            this.properties.put("title", ts.getTranslatedString("module.visualization.connection-wizard-select-link-endpoints"));
                    
            HierarchicalDataProvider dataProviderSourceTree = buildTreeHierarchicalDataProvider((BusinessObjectLight)properties.get("rootASide"));
            HierarchicalDataProvider dataProviderTargetTree = buildTreeHierarchicalDataProvider((BusinessObjectLight)properties.get("rootBSide"));
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
                    
                    Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                    String newConnection = physicalConnectionsService.createPhysicalConnection(selectedEndPointA.getClassName(), selectedEndPointA.getId(), selectedEndPointB.getClassName(), 
                            selectedEndPointB.getId(), properties.getProperty("name"), properties.getProperty("class"), 
                            (String)properties.get("templateId"), session.getUser().getUserName());
                    
                    properties.put("connection", new BusinessObjectLight(properties.getProperty("class"), newConnection, properties.getProperty("name")));
                    
                    return null;
                }
            } catch (IllegalStateException | InvalidArgumentException | MetadataObjectNotFoundException | OperationNotPermittedException ex) {
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
                        Logger.getLogger(SelectContainerEndpointsStep.class.getName()).log(Level.SEVERE, null, ex);
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
