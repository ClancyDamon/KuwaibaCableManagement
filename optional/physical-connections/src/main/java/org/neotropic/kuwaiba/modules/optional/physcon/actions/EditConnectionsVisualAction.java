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
package org.neotropic.kuwaiba.modules.optional.physcon.actions;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.NavigationTree;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.nodes.InventoryObjectNode;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsModule;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.icons.BasicTreeNodeIconGenerator;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Widget that allows editing connection end points
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class EditConnectionsVisualAction extends AbstractVisualAdvancedAction {
    /**
     * Utility class that help to load resources like icons and images.
     */
    @Autowired
    private ResourceFactory resourceFactory;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the connection service.
     */
    @Autowired
    private PhysicalConnectionsService physicalConnectionsService;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    
    @Autowired
    private EditConnectionsAction editConnectionsAction;

    private BusinessObjectLight selectedEndPointA;
    private BusinessObjectLight selectedEndPointB;
    private BusinessObjectLight currentEnpointA;
    private BusinessObjectLight currentEnpointB;
    private Label lblCurrentEndPointA;
    private Label lblCurrentEndPointB;
    private Label lblCurrentConnection;
    private BusinessObjectLight selectedPhysicalConnection;
    TreeGrid<InventoryObjectNode> aSideTree ;
    TreeGrid<InventoryObjectNode> bSideTree ;
    TreeGrid<InventoryObjectNode> containerTree ;
    boolean isContainer;
      
    @Autowired
    private ResourceFactory rs;

    public EditConnectionsVisualAction() {
        super(PhysicalConnectionsModule.MODULE_ID);
    }
    
    
    @Override
    public String appliesTo() {
        return Constants.CLASS_GENERICPHYSICALCONNECTION;
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            if (!parameters.containsKey("businessObject"))
                return null;
            BusinessObjectLight businessObject = (BusinessObjectLight) parameters.get("businessObject");
            isContainer = mem.isSubclassOf("GenericPhysicalContainer", businessObject.getClassName());
            selectedPhysicalConnection = businessObject;
            
            aSideTree = new NavigationTree(new BasicTreeNodeIconGenerator(rs));
            bSideTree = new NavigationTree(new BasicTreeNodeIconGenerator(rs));
            aSideTree.setHeightByRows(isContainer);
            bSideTree.setHeightByRows(isContainer);
            containerTree = new NavigationTree(new BasicTreeNodeIconGenerator(rs));
            containerTree.setHeightByRows(true);
            
            HierarchicalDataProvider dataProviderConnectionsTree = buildTreeHierarchicalDataProvider(selectedPhysicalConnection, true);
            containerTree.setDataProvider(dataProviderConnectionsTree);  
            
            
            Label lblSelectedEndPointATitle = new BoldLabel(ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.current-endpointa"));
            Label lblSelectedEndPointBTitle = new BoldLabel(ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.current-endpointb"));
            Label lblSelectedConnection = new BoldLabel(ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.current-link"));
            lblCurrentEndPointA = new Label();
            lblCurrentEndPointB = new Label();
            lblCurrentConnection = new Label();
            HorizontalLayout lytSelectedEndPointA = new HorizontalLayout(lblSelectedEndPointATitle, lblCurrentEndPointA);
            HorizontalLayout lytSelectedEndPointB = new HorizontalLayout(lblSelectedEndPointBTitle, lblCurrentEndPointB);
            lytSelectedEndPointA.setPadding(false);
            lytSelectedEndPointB.setPadding(false);
            aSideTree.addItemClickListener( item -> {
                selectedEndPointA = item.getItem().getObject().equals(selectedEndPointA) ? null : item.getItem().getObject();
            });
            bSideTree.addItemClickListener( item -> {
                selectedEndPointB = item.getItem().getObject().equals(selectedEndPointB) ? null : item.getItem().getObject();
            });
            
            
            Button btnConnectSelectedEndpoints = new Button(ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.connect-selected-endpoints"),
                    VaadinIcon.CONNECT_O.create(), evt -> {
                        try {
                            connectSelectedEndpoints();
                        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | OperationNotPermittedException | InvalidArgumentException ex) {
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                                    AbstractNotification.NotificationType.ERROR, ts).open();
                        }
                    });
            btnConnectSelectedEndpoints.setWidthFull();
            btnConnectSelectedEndpoints.setEnabled(!isContainer);
            Button btnDisconectBothSides = new Button(ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.disconnect-both-sides"),
                    VaadinIcon.UNLINK.create(), evt ->  {
                        try {
                            bem.releaseRelationships(selectedPhysicalConnection.getClassName(), selectedPhysicalConnection.getId(), Arrays.asList("endpointA", "endpointB")); //NOI18N
                            currentEnpointA = null;
                            currentEnpointB = null;
                            updateEndpointLabels();
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.endpoints-disconnected"),
                                    AbstractNotification.NotificationType.INFO, ts).open();
                        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                                    AbstractNotification.NotificationType.ERROR, ts).open();
                        }
                    });
            btnDisconectBothSides.setEnabled(!isContainer);
            Button btnDisconectSideA = new Button(ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.disconnect-endpointa"),
                    VaadinIcon.LEVEL_LEFT.create(), evt ->  {
                        try {
                            bem.releaseRelationships(selectedPhysicalConnection.getClassName(), selectedPhysicalConnection.getId(), Arrays.asList("endpointA")); //NOI18N
                            currentEnpointA = null;
                            updateEndpointLabels();
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.endpoint-disconnected"),
                                    AbstractNotification.NotificationType.INFO, ts).open();
                        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                                    AbstractNotification.NotificationType.ERROR, ts).open();
                        }
                    });
            btnDisconectSideA.setEnabled(!isContainer);
            Button btnDisconectSideB = new Button(ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.disconnect-endpointb"),
                    VaadinIcon.LEVEL_RIGHT.create(), evt ->  {
                        try {
                            bem.releaseRelationships(selectedPhysicalConnection.getClassName(), selectedPhysicalConnection.getId(), Arrays.asList("endpointB")); //NOI18N
                            currentEnpointB = null;
                            updateEndpointLabels();
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.endpoint-disconnected"),
                                    AbstractNotification.NotificationType.INFO, ts).open();
                        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                                    AbstractNotification.NotificationType.ERROR, ts).open();
                        }
                    });
            btnDisconectSideB.setEnabled(!isContainer);
            containerTree.addItemClickListener( item -> {
                if (item.getItem().getObject() != null) {
                    if (!item.getItem().getObject().equals(businessObject)) {
                        selectedPhysicalConnection =  item.getItem().getObject();
                        loadCurrentEndPoints();
                        try {
                            updateEndpointLabels();
                        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                                    ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                            Logger.getLogger(EditConnectionsVisualAction.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        btnConnectSelectedEndpoints.setEnabled(true);
                        btnDisconectBothSides.setEnabled(true);
                        btnDisconectSideA.setEnabled(true);
                        btnDisconectSideB.setEnabled(true);
                    }
                }
            });
            
            HorizontalLayout lytEndpointA = new HorizontalLayout(lblSelectedEndPointATitle, lblCurrentEndPointA, btnDisconectSideA);
            HorizontalLayout lytEndpointB = new HorizontalLayout(lblSelectedEndPointBTitle, lblCurrentEndPointB, btnDisconectSideB);
            lytEndpointA.setFlexGrow(1, lblCurrentEndPointA);
            lytEndpointB.setFlexGrow(1, lblCurrentEndPointB);
            lytEndpointA.setWidthFull();
            lytEndpointB.setWidthFull();
            
            VerticalLayout lytActions = new VerticalLayout(btnConnectSelectedEndpoints, btnDisconectBothSides);
            lytActions.setWidthFull();
            lytActions.setAlignItems(FlexComponent.Alignment.STRETCH);
            
            VerticalLayout lytSideA = new VerticalLayout(aSideTree);
            VerticalLayout lytSideB = new VerticalLayout(bSideTree);
            lytSideA.setMargin(false);
            lytSideA.setPadding(false);
            lytSideB.setMargin(false);
            lytSideB.setPadding(false);
            lytSideA.getElement().getStyle().set("overflow-x", "scroll");
            lytSideB.getElement().getStyle().set("overflow-x", "scroll");
            
            VerticalLayout lytContainerTree = new VerticalLayout(lblSelectedConnection,
                    lblCurrentConnection, containerTree, lytActions);
            lytContainerTree.setSpacing(false);
            lytContainerTree.setFlexGrow(1, containerTree);
            HorizontalLayout lytTrees = new HorizontalLayout();
            lytTrees.add(lytSideA, lytContainerTree, lytSideB);
            lytTrees.setMinHeight("350px");
            lytTrees.setWidthFull();
            lytTrees.setPadding(false);
            buildTrees();
            if (!isContainer)
                updateEndpointLabels();
            lytTrees.setSpacing(false);
            
            VerticalLayout lytContent = new VerticalLayout();
            lytContent.add(lytTrees, lytEndpointA, lytEndpointB);
            lytContent.setSizeFull();
            lytContent.setSpacing(false);
            lytContent.setMargin(false);
            lytContent.setPadding(false);
            ConfirmDialog dlgAction = new ConfirmDialog(ts, ts.getTranslatedString("module.physcon.actions.edit-connections.name"), "");
            dlgAction.setWidth("95%");
            dlgAction.setContent(lytContent);
            dlgAction.getBtnConfirm().setVisible(false);
            dlgAction.getBtnCancel().setText(ts.getTranslatedString("module.general.messages.close"));
            return dlgAction;
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            Logger.getLogger(EditConnectionsVisualAction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
      /**
     * Function that creates a new HierarchicalDataProvider for a tree grid.
     * @param root the main toot of the tree
     * @param specialChildren true if special childrens are needed, false to normal childrens
     * @return the new data Provider the the given root
     */
    public HierarchicalDataProvider buildTreeHierarchicalDataProvider(BusinessObjectLight root, boolean specialChildren) {
        return new AbstractBackEndHierarchicalDataProvider<InventoryObjectNode, Void>() {
            @Override
            protected Stream<InventoryObjectNode> fetchChildrenFromBackEnd(HierarchicalQuery<InventoryObjectNode, Void> query) {
                InventoryObjectNode parent = query.getParent();
                if (parent != null) {
                    if (parent.getObject().getClassName() == null || parent.getObject().getClassName().isEmpty()) 
                        return new ArrayList().stream();
                    BusinessObjectLight object = parent.getObject();
                    try {
                        List<BusinessObjectLight> children;
                        if (specialChildren)
                            children = bem.getObjectSpecialChildren(object.getClassName(), 
                                object.getId() == null ? "-1" : object.getId());
                        else
                            children = bem.getObjectChildren(object.getClassName(), 
                                object.getId() == null ? "-1" : object.getId(), -1);
                        List<InventoryObjectNode> theChildren = new ArrayList();
                        for (BusinessObjectLight child : children)
                            theChildren.add(new InventoryObjectNode(child));
                        return theChildren.stream();
                    } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
                        Notification.show(ex.getMessage());
                        return new ArrayList().stream();
                    } 
                } else                          
                        return Arrays.asList(new InventoryObjectNode(root)).stream();                   
                             
            }

            @Override
            public int getChildCount(HierarchicalQuery<InventoryObjectNode, Void> query) {
                InventoryObjectNode parent = query.getParent();
                if (parent != null) {
                    try {
                        BusinessObjectLight object = parent.getObject();
                        if (specialChildren)
                            return bem.getObjectSpecialChildren(object.getClassName(), 
                                object.getId() == null ? "-1" : object.getId()).size();
                        else 
                            return (int) bem.getObjectChildrenCount(object.getClassName(), object.getId(), null);
                    } catch (InvalidArgumentException | BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
                          Logger.getLogger(EditConnectionsVisualAction.class.getName()).log(Level.SEVERE, null, ex);
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
    
    /**
     * Set the title/tool tip for the given button
     * @param button the button to be set
     * @param title the title to be added
     */
    public void setButtonTitle(Button button, String title) {
        button.getElement().setProperty("title", title);     
    }
    /**
     * Connect the current selected endpoint A and endpoint B. Previous validations are made,
     * such as checking if they are ports or if they are related to another connection.
     * @throws MetadataObjectNotFoundException
     * @throws BusinessObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws InvalidArgumentException 
     */
    private void connectSelectedEndpoints() throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException, 
            InvalidArgumentException {

        boolean container = mem.isSubclassOf("GenericPhysicalContainer", selectedPhysicalConnection.getClassName());
        if (selectedEndPointA == null && selectedEndPointB == null) {

            new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                    ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.select-port-nodes"),
                    AbstractNotification.NotificationType.WARNING, ts).open();
            return;
        }

        if (!container && selectedEndPointA != null && !mem.isSubclassOf(Constants.CLASS_GENERICPORT, selectedEndPointA.getClassName())) {

            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    String.format(ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.class-isnot-port"),
                            selectedEndPointA.getClassName()),
                    AbstractNotification.NotificationType.ERROR, ts).open();
            return;
        }
        if (!container &&  selectedEndPointB != null && !mem.isSubclassOf(Constants.CLASS_GENERICPORT, selectedEndPointB.getClassName())) {

            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    String.format(ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.class-isnot-port"),
                            selectedEndPointB.getClassName()),
                    AbstractNotification.NotificationType.ERROR, ts).open();
            return;
        }

        if (selectedEndPointA != null & Objects.equals(selectedEndPointA, selectedEndPointB)) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    String.format(ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.cannot-connect-port-itself"),
                            selectedEndPointB.getClassName()),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }

        String endpointAName = "endpointA", endpointBName = "endpointB";

        if (selectedEndPointA != null && !selectedEndPointA.equals(currentEnpointA)) {
            if (currentEnpointA != null) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                                ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.connection-already-has-endpointa"),
                        AbstractNotification.NotificationType.WARNING, ts).open();
                
            } else {
                if (!container && (!bem.getSpecialAttribute(selectedEndPointA.getClassName(), selectedEndPointA.getId(), endpointAName).isEmpty() || 
                        !bem.getSpecialAttribute(selectedEndPointA.getClassName(), selectedEndPointA.getId(), endpointBName).isEmpty())) {
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                                    ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.selected-endpointa-already-connected"),  
                                    AbstractNotification.NotificationType.WARNING, ts).open();
                            return;
                }

                if (selectedEndPointA.equals(currentEnpointB)) {

                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            String.format(ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.new-endpointa-is-endpointb"),
                                    selectedEndPointB.getClassName()),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                    return;
                }
                bem.createSpecialRelationship(selectedPhysicalConnection.getClassName(), selectedPhysicalConnection.getId(),
                        selectedEndPointA.getClassName(), selectedEndPointA.getId(), endpointAName, true);
                currentEnpointA = selectedEndPointA;
                updateEndpointLabels();
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                        ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.endpointa-connected"),
                         AbstractNotification.NotificationType.INFO, ts).open();
            }
        }
        if (selectedEndPointB != null && !selectedEndPointB.equals(currentEnpointB)) {
            
            if (currentEnpointB != null) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                                ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.connection-already-has-endpointb"), 
                        AbstractNotification.NotificationType.WARNING, ts).open();
                
            } else {
            
                if (!container && (!bem.getSpecialAttribute(selectedEndPointB.getClassName(), selectedEndPointB.getId(), endpointAName).isEmpty() || 
                        !bem.getSpecialAttribute(selectedEndPointB.getClassName(), selectedEndPointB.getId(), endpointBName).isEmpty())) {
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                                    ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.selected-endpointb-already-connected"),  
                                    AbstractNotification.NotificationType.WARNING, ts).open();
                            return;
                }
                if (selectedEndPointB.equals(currentEnpointA)) {

                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            String.format(ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.new-endpointb-is-endpointa"),
                                    selectedEndPointB.getClassName()),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                    return;
                }
                bem.createSpecialRelationship(selectedPhysicalConnection.getClassName(), selectedPhysicalConnection.getId(),
                        selectedEndPointB.getClassName(), selectedEndPointB.getId(), endpointBName, true);
                currentEnpointB = selectedEndPointB;
                updateEndpointLabels();
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                        ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.endpointb-connected"),
                         AbstractNotification.NotificationType.INFO, ts).open();
            }
        }
    }

    private void updateEndpointLabels() throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        if (currentEnpointA == null)
            lblCurrentEndPointA.setText(ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.disconnected"));
        else {
            List<BusinessObjectLight> parents = bem.getParents(currentEnpointA.getClassName(), currentEnpointA.getId());
            String path = "";
            for (int i = 0; i < parents.size() && i < 4; i++) 
                path +=  "/" + parents.get(i).getName() ;
            lblCurrentEndPointA.setText(currentEnpointA.getName() + path);
        }
        if (currentEnpointB == null)
            lblCurrentEndPointB.setText(ts.getTranslatedString("module.visualization.edit-connection-endpoints-view.disconnected"));
        else {
            List<BusinessObjectLight> parents = bem.getParents(currentEnpointB.getClassName(), currentEnpointB.getId());
            String path = "";
            for (int i = 0; i < parents.size() && i < 4; i++) 
                path +=  " / " + parents.get(i).toString() ;
            lblCurrentEndPointB.setText(currentEnpointB.getName() + path);
        }   
       
        lblCurrentConnection.setText(lblCurrentConnection == null ? "" : selectedPhysicalConnection.getName());
    }
    
    private void buildTrees() throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        selectedEndPointA = null;
        selectedEndPointB = null;
        currentEnpointA = null;
        currentEnpointB = null;
        loadCurrentEndPoints();
        
        BusinessObjectLight rootParentA = null, rootParentB = null;
       
        if (currentEnpointA != null && !isContainer) 
            rootParentA = bem.getFirstParentOfClass(currentEnpointA.getClassName(), currentEnpointA.getId(), "Building");
        else
            rootParentA = currentEnpointA;
        if (rootParentA == null)
            rootParentA = new BusinessObjectLight(Constants.DUMMY_ROOT, null, Constants.DUMMY_ROOT);
        if (currentEnpointB != null && !isContainer) 
            rootParentB = bem.getFirstParentOfClass(currentEnpointB.getClassName(), currentEnpointB.getId(), "Building");
        else
            rootParentB = currentEnpointB;
        if (rootParentB == null) 
            rootParentB = new BusinessObjectLight(Constants.DUMMY_ROOT, null, Constants.DUMMY_ROOT);
        
        HierarchicalDataProvider dataProviderSourceTree = buildTreeHierarchicalDataProvider(rootParentA, false);
        HierarchicalDataProvider dataProviderTargetTree = buildTreeHierarchicalDataProvider(rootParentB, false);
        
        aSideTree.setDataProvider(dataProviderSourceTree);
        bSideTree.setDataProvider(dataProviderTargetTree);
        
    }

    private void loadCurrentEndPoints()  {
        try {
            Map<String, List<BusinessObjectLight>> attrs = bem.getSpecialAttributes(selectedPhysicalConnection.getClassName(), selectedPhysicalConnection.getId(), "endpointA", "endpointB");
            if (attrs.containsKey("endpointA"))
                currentEnpointA = (BusinessObjectLight) attrs.get("endpointA").get(0);
           else
                currentEnpointA = null;
            if (attrs.containsKey("endpointB"))
                currentEnpointB = (BusinessObjectLight) attrs.get("endpointB").get(0);
            else 
                currentEnpointB = null;
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
             new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
            Logger.getLogger(EditConnectionsVisualAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int getRequiredSelectedObjects() {
         return 1;
    }

    @Override
    public AbstractAction getModuleAction() {
       return editConnectionsAction;
    }

}
