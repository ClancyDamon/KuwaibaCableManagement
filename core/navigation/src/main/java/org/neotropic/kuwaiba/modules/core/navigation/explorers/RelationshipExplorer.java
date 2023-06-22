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
package org.neotropic.kuwaiba.modules.core.navigation.explorers;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractExplorerWidget;
import org.neotropic.kuwaiba.core.apis.integration.views.ExplorerWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.ObjectOptionsPanel;
import org.neotropic.kuwaiba.modules.core.navigation.actions.ShowMoreInformationAction;
import org.neotropic.kuwaiba.modules.core.navigation.explorers.grids.IconRelationshipCellGrid;
import org.neotropic.kuwaiba.modules.core.navigation.explorers.nodes.RelationshipExplorerNode;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.grids.IconNameCellGrid;
import org.neotropic.util.visual.icons.ClassNameIconGenerator;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * An explorer that allows the user to see the relationships of an inventory object.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@CssImport("./styles/explorer.css")
@Component
public class RelationshipExplorer extends AbstractExplorerWidget<VerticalLayout> {
    /**
     * Reference to the action registry.
     */
    @Autowired
    private CoreActionsRegistry coreActionRegistry;
    /**
     * Reference to the action registry.
     */
    @Autowired
    private AdvancedActionsRegistry advancedActionsRegistry;
    /**
     * All the object-related views exposed by other modules.
     */
    @Autowired
    private ViewWidgetRegistry viewWidgetRegistry;
    /**
     * All the registered explorers.
     */
    @Autowired
    private ExplorerWidgetRegistry explorerWidgetRegistry;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Factory to build resources from data source.
     */
    @Autowired
    private ResourceFactory resourceFactory;
    /**
     * The window to show more information about an object.
     */
    @Autowired
    private ShowMoreInformationAction windowMoreInformation;
    /**
     * Navigation tree of relationships
     */
    private TreeGrid<RelationshipExplorerNode> navTree;
    /**
     * Object to save the current object
     */
    private BusinessObjectLight currentObject;
    /**
     * Main layout
     */
    private VerticalLayout lytMain;
    /**
     * The right-side panel displaying the property sheet of the selected object plus some other options.
     */
    private VerticalLayout lytDetailsPanel;
    /**
     * An icon generator for create icons
     */
    private ClassNameIconGenerator iconGenerator;
    /**
     * Object to show more information about business object
     */
    private Button btnInfo;

    @Override
    public String getName() {
        return ts.getTranslatedString("module.navigation.explorers.relationship.title");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.navigation.explorers.relationship.description");
    }

    @Override
    public String appliesTo() {
        return "InventoryObject";
    }

    @Override
    public VerticalLayout build(BusinessObjectLight selectedObject) {
        iconGenerator = new ClassNameIconGenerator(resourceFactory);
        currentObject = selectedObject;
        // Main Layout
        lytMain = new VerticalLayout();
        // Header
        Label lblTitle = new Label(String.format(ts.getTranslatedString("module.navigation.explorers.relationship.header"), selectedObject.toString()));
        lblTitle.setClassName("dialog-title");
        HorizontalLayout lytTitleHeader = new HorizontalLayout(lblTitle);
        lytTitleHeader.setPadding(false);
        lytTitleHeader.setMargin(false);
        // Add info to layout
        Label lblInfo = new Label(ts.getTranslatedString("module.navigation.explorers.help"));
        lblInfo.setClassName("info-label");
        HorizontalLayout lytInfo = new HorizontalLayout(lblInfo);
        lytInfo.setMargin(false);
        lytInfo.setPadding(false);
        // Navigation Tree
        loadNavigationTree(selectedObject);
        VerticalLayout lytNavTree = new VerticalLayout(navTree);
        lytNavTree.setHeightFull();
        lytNavTree.setWidth("50%");
        lytNavTree.setSpacing(false);
        lytNavTree.setMargin(false);
        lytNavTree.setPadding(false);
        // Details Panel
        lytDetailsPanel = new VerticalLayout();
        lytDetailsPanel.setId("lyt-details-panel");
        lytDetailsPanel.setSpacing(false);
        lytDetailsPanel.setMargin(false);
        lytDetailsPanel.setHeightFull();
        lytDetailsPanel.setWidth("60%");
        lytDetailsPanel.setClassName("button-more-info-position");
        buildDetailsPanel(currentObject);
        // Content
        HorizontalLayout lytContent = new HorizontalLayout(lytNavTree, lytDetailsPanel);
        lytContent.setWidthFull();
        lytContent.setHeightFull();
        // Add content to layout
        lytMain.add(lytTitleHeader, lytInfo, lytContent);
        lytMain.setHeightFull();
        lytMain.setWidthFull();
        lytMain.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, lytTitleHeader);
        return lytMain;
    }
    
    private void loadNavigationTree(BusinessObjectLight selectedObject) {
        try {
            navTree = new TreeGrid<>();
            navTree.setHeightFull();
            navTree.setWidthFull();
            navTree.setSelectionMode(Grid.SelectionMode.SINGLE);
            RelationshipExplorerNode root = new RelationshipExplorerNode(bem.getObjectLight(selectedObject.getClassName(), selectedObject.getId()));
            navTree.setDataProvider(getDataProvider(root));

            navTree.addComponentHierarchyColumn(item -> {
                if (null == item.getType())
                    return new Label(item.toString());
                else {
                    switch (item.getType()) {
                        case BUSINESS_OBJECT:
                            FormattedObjectDisplayNameSpan itemName = new FormattedObjectDisplayNameSpan(
                                    item.getBusinessObject(), false, false, true, false);

                            return new IconNameCellGrid(itemName, item.getBusinessObject().getClassName(), iconGenerator);
                        case RELATIONSHIP:
                            return new IconRelationshipCellGrid(item.getRelationship(), resourceFactory);
                        default:
                            return new Label(item.toString());
                    }
                }
            });

            navTree.addItemClickListener(event -> {
                if (event.getItem().getType().equals(RelationshipExplorerNode.RelationshipExplorerNodeType.BUSINESS_OBJECT)) {
                    btnInfo.setEnabled(true);
                    currentObject = event.getItem().getBusinessObject();
                    buildDetailsPanel(currentObject);
                    lytDetailsPanel.setVisible(true);
                } else {
                    btnInfo.setEnabled(false);
                    lytDetailsPanel.setVisible(false);
                }
            });
            navTree.expand(root);
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    private HierarchicalDataProvider getDataProvider(RelationshipExplorerNode root) {
        return new AbstractBackEndHierarchicalDataProvider() {
            @Override
            protected Stream fetchChildrenFromBackEnd(HierarchicalQuery query) {
                RelationshipExplorerNode parent = (RelationshipExplorerNode) query.getParent();
                try {
                    List<RelationshipExplorerNode> relationshipExplorerNode = new ArrayList<>();
                    if (parent != null) {
                        if (parent.getType() == RelationshipExplorerNode.RelationshipExplorerNodeType.RELATIONSHIP) {
                            if (parent.getRelationship().equals("parent")) {
                                BusinessObjectLight objectParent =
                                    bem.getParent(parent.getRelationshipParent().getClassName(), parent.getRelationshipParent().getId());
                                
                                if (!objectParent.getClassName().equals(Constants.DUMMY_ROOT))
                                    relationshipExplorerNode.add(new RelationshipExplorerNode(objectParent));
                            } else {
                                List<BusinessObjectLight> relationships
                                        = bem.getSpecialAttribute(parent.getRelationshipParent().getClassName(),
                                                parent.getRelationshipParent().getId(), parent.getRelationship());

                                relationships.forEach(relationship -> relationshipExplorerNode.add(
                                        new RelationshipExplorerNode(relationship)));
                            }
                        } else if (parent.getType() == RelationshipExplorerNode.RelationshipExplorerNodeType.BUSINESS_OBJECT) {
                            HashMap<String, List<BusinessObjectLight>> relationships =
                                    bem.getSpecialAttributes(parent.getBusinessObject().getClassName(), parent.getBusinessObject().getId());
                            
                            relationshipExplorerNode.add(new RelationshipExplorerNode("parent", parent.getBusinessObject()));
                            
                            relationships.keySet().forEach(relationship -> relationshipExplorerNode.add(
                                    new RelationshipExplorerNode(relationship, parent.getBusinessObject())));         
                        }
                         return relationshipExplorerNode.stream().skip(query.getOffset()).limit(query.getLimit());
                    } else
                        relationshipExplorerNode.add(root);

                    return relationshipExplorerNode.stream();
                } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                    return new ArrayList().stream();
                }
            }

            @Override
            public int getChildCount(HierarchicalQuery query) {
                RelationshipExplorerNode parent = (RelationshipExplorerNode) query.getParent();
                try {
                    List<RelationshipExplorerNode> relationshipExplorerNode = new ArrayList<>();
                    if (parent != null) {
                        if (parent.getType() == RelationshipExplorerNode.RelationshipExplorerNodeType.RELATIONSHIP) {
                            if (parent.getRelationship().equals("parent")) {
                                BusinessObjectLight objectParent
                                        = bem.getParent(parent.getRelationshipParent().getClassName(), parent.getRelationshipParent().getId());
                                
                                if (!objectParent.getClassName().equals(Constants.DUMMY_ROOT))
                                    relationshipExplorerNode.add(new RelationshipExplorerNode(objectParent));
                            } else {
                                List<BusinessObjectLight> relationships
                                        = bem.getSpecialAttribute(parent.getRelationshipParent().getClassName(),
                                                parent.getRelationshipParent().getId(), parent.getRelationship());

                                relationships.forEach(relationship -> relationshipExplorerNode.add(
                                        new RelationshipExplorerNode(relationship)));
                            }
                        } else if (parent.getType() == RelationshipExplorerNode.RelationshipExplorerNodeType.BUSINESS_OBJECT) {
                            HashMap<String, List<BusinessObjectLight>> relationships
                                    = bem.getSpecialAttributes(parent.getBusinessObject().getClassName(), parent.getBusinessObject().getId());

                            relationshipExplorerNode.add(new RelationshipExplorerNode("parent", parent.getBusinessObject()));

                            relationships.keySet().forEach(relationship -> relationshipExplorerNode.add(
                                    new RelationshipExplorerNode(relationship, parent.getBusinessObject())));
                        }
                        return (int) relationshipExplorerNode.stream().skip(query.getOffset()).limit(query.getLimit()).count();
                    } else
                        relationshipExplorerNode.add(root);
                    
                    return (int) relationshipExplorerNode.stream().count();
                } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                    return 0;
                }
            }

            @Override
            public boolean hasChildren(Object object) {
                return object instanceof RelationshipExplorerNode;
            }
        };
    }
    
    /**
     * Creates the right-most layout with the options for the selected object
     * @param selectedObject the selected object in the nav tree
     */
    private void buildDetailsPanel(BusinessObjectLight selectedObject) {
        try {
            lytDetailsPanel.removeAll();
            if (!selectedObject.getClassName().equals(Constants.DUMMY_ROOT)) {
                ObjectOptionsPanel pnlOptions = new ObjectOptionsPanel(selectedObject,
                        coreActionRegistry, advancedActionsRegistry, viewWidgetRegistry, explorerWidgetRegistry, mem, aem, bem, ts);
                pnlOptions.setShowViews(false);
                pnlOptions.setShowExplorers(false);
                pnlOptions.setSelectionListener((event) -> {
                    switch (event.getActionCommand()) {
                        case ObjectOptionsPanel.EVENT_ACTION_SELECTION:
                            ModuleActionParameterSet parameters = new ModuleActionParameterSet(new ModuleActionParameter<>("businessObject", selectedObject));
                            Dialog wdwObjectAction = (Dialog) ((AbstractVisualInventoryAction) event.getSource()).getVisualComponent(parameters);
                            wdwObjectAction.open();
                            break;
                    }
                });
                pnlOptions.setPropertyListener((property) -> {
                    HashMap<String, String> attributes = new HashMap<>();
                    Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                    Object lastValue =  pnlOptions.lastValue(property.getName());
                    attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));
                    try {
                        bem.updateObject(selectedObject.getClassName(), selectedObject.getId(), attributes);
                        if(property.getName().equals(Constants.PROPERTY_NAME))
                            refreshGrids();
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                                ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                                AbstractNotification.NotificationType.INFO, ts).open();
                        // activity log
                        aem.createObjectActivityLogEntry(session.getUser().getUserName(), selectedObject.getClassName(),
                                selectedObject.getId(), ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT,
                                property.getName(), lastValue == null ? "" : lastValue.toString(), property.getAsString(), "");
                    } catch (InventoryException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                                ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                        pnlOptions.UndoLastEdit();
                    }
                });
                // Action show more information
                btnInfo = new Button(this.windowMoreInformation.getDisplayName());
                btnInfo.setWidthFull();
                btnInfo.setMaxWidth("450px");
                btnInfo.addClickListener(event -> {
                    this.windowMoreInformation.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("object", currentObject))).open();
                });
                // Add content to layout
                lytDetailsPanel.add(btnInfo, pnlOptions.build(UI.getCurrent().getSession().getAttribute(Session.class).getUser()));
            }
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    /**
     * Refresh the grids if an attributes is update
     * @param className the updated class name of the object
     */
    private void refreshGrids() {
        if (navTree != null)
            navTree.getDataProvider().refreshAll();
    }
}