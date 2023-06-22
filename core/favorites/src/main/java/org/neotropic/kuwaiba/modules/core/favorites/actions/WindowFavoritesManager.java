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
package org.neotropic.kuwaiba.modules.core.favorites.actions;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractExplorerWidget;
import org.neotropic.kuwaiba.core.apis.integration.views.ExplorerWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.FavoritesFolder;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfile;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.favorites.nodes.FavoritesManagerNode;
import org.neotropic.kuwaiba.modules.core.navigation.ObjectOptionsPanel;
import org.neotropic.kuwaiba.modules.core.navigation.actions.CopyBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.DefaultDeleteBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.MoveBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectFromTemplateVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewMultipleBusinessObjectsVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.ShowMoreInformationAction;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.grids.IconNameCellGrid;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.icons.ClassNameIconGenerator;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Window to manage favorites folder.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class WindowFavoritesManager extends ConfirmDialog implements ActionCompletedListener {
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private final TranslationService ts;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private final ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private final BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private final MetadataEntityManager mem;
    /**
     * The visual action to create a new favorite folder
     */
    @Autowired
    private final NewFavoriteVisualAction newFavoriteVisualAction;
    /**
     * The visual action to delete a favorite folder
     */
    @Autowired
    private final DeleteFavoriteVisualAction deleteFavoriteVisualAction;
    /**
     * Factory to build resources from data source.
     */
    @Autowired
    private final ResourceFactory resourceFactory;
    /**
     * Reference to the action registry.
     */
    @Autowired
    private final CoreActionsRegistry coreActionsRegistry;
    /**
     * Reference to the action registry.
     */
    @Autowired
    private final AdvancedActionsRegistry advancedActionsRegistry;
    /**
     * All the object-related views exposed by other modules.
     */
    @Autowired
    private final ViewWidgetRegistry viewWidgetRegistry;
    /**
     * All the registered explorers.
     */
    @Autowired
    private final ExplorerWidgetRegistry explorerWidgetRegistry;
    /**
     * Reference to the action that creates a new Business Object.
     */
    @Autowired
    private final NewBusinessObjectVisualAction newBusinessObjectVisualAction;
    /**
     * Reference to the action that creates a new Business Object from a template.
     */
    @Autowired
    private final NewBusinessObjectFromTemplateVisualAction newBusinessObjectFromTemplateVisualAction;
    /**
     * Reference to the action that creates a multiple new Business Object from a pattern.
     */
    @Autowired
    private final NewMultipleBusinessObjectsVisualAction newMultipleBusinessObjectsVisualAction;
    /**
     * Reference to the action that deletes a Business Object.
     */
    @Autowired
    private final DefaultDeleteBusinessObjectVisualAction deleteBusinessObjectVisualAction;
    /**
     * Reference to the action that copies a business object to another business object.
     */
    @Autowired
    private final CopyBusinessObjectVisualAction copyBusinessObjectVisualAction;
    /**
     * Reference to the action that moves a business object to another business object.
     */
    @Autowired
    private final MoveBusinessObjectVisualAction moveBusinessObjectVisualAction;
    /**
     * The window to show more information about an object.
     */
    @Autowired
    private final ShowMoreInformationAction windowMoreInformation;
    /**
     * Items limit. -1 To return all
     */
    private static final int LIMIT = -1;
    /**
     * Layouts
     */
    private VerticalLayout lytContent;
    private VerticalLayout lytFolder;
    private HorizontalLayout lytFolderActions;
    private HorizontalLayout lytObjects; 
    private VerticalLayout lytDetailsPanel;
    /**
     * Object to save the selected folder
     */
    private FavoritesFolder currentFolder;
    /**
     * Object to save the selected object
     */
    private BusinessObjectLight currentObject;
    /**
     * Object to edit the selected folder
     */
    private ActionButton btnEditFolder;
    /**
     * Object to delete the selected folder
     */
    private ActionButton btnDeleteFolder;
    /**
     * An icon generator for create icons
     */
    private ClassNameIconGenerator iconGenerator;
    /**
     * The grid with the list objects
     */
    private TreeGrid<FavoritesManagerNode> tblObjects;
    /**
     * Boolean used to validate actions
     */
    private Boolean isFolder = false;
    
    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            if (isFolder == true)
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                        AbstractNotification.NotificationType.INFO, ts).open();
            else if (isFolder == false) {
                if (ev.getActionResponse().containsKey(ActionResponse.ActionType.REMOVE))
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                            ts.getTranslatedString("module.navigation.actions.delete-business-object.name-success"),
                            AbstractNotification.NotificationType.INFO, ts).open();
                else if (ev.getActionResponse().containsKey(ActionResponse.ActionType.ADD)
                        && ev.getActionResponse().containsKey(Constants.PROPERTY_PARENT_CLASS_NAME)) {
                    Object addedObj = ev.getActionResponse().get(ActionResponse.ActionType.ADD);

                    if (addedObj instanceof BusinessObjectLight) //notification for single object creation 
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                                ts.getTranslatedString("module.navigation.actions.new-business-object.name-success"),
                                AbstractNotification.NotificationType.INFO, ts).open();
                    else if (addedObj instanceof Integer) //notification for bulk object creation 
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                                String.format(ts.getTranslatedString("module.navigation.actions.new-business-objects.name-success"), addedObj),
                                AbstractNotification.NotificationType.INFO, ts).open();
                }
                tblObjects.getDataProvider().refreshAll();
            }
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
    }
  
    public WindowFavoritesManager(TranslationService ts, 
            ApplicationEntityManager aem, 
            BusinessEntityManager bem,
            MetadataEntityManager mem,
            CoreActionsRegistry coreActionsRegistry,
            AdvancedActionsRegistry advancedActionsRegistry,
            ViewWidgetRegistry viewWidgetRegistry,
            ExplorerWidgetRegistry explorerWidgetRegistry,
            NewBusinessObjectVisualAction newBusinessObjectVisualAction,
            NewBusinessObjectFromTemplateVisualAction newBusinessObjectFromTemplateVisualAction,
            NewMultipleBusinessObjectsVisualAction newMultipleBusinessObjectsVisualAction,
            DefaultDeleteBusinessObjectVisualAction deleteBusinessObjectVisualAction,
            CopyBusinessObjectVisualAction copyBusinessObjectVisualAction,
            MoveBusinessObjectVisualAction moveBusinessObjectVisualAction,
            ShowMoreInformationAction showMoreInformationAction,
            NewFavoriteVisualAction newFavoriteVisualAction,
            DeleteFavoriteVisualAction deleteFavoriteVisualAction,
            ResourceFactory resourceFactory) {
        
        this.ts = ts;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.resourceFactory = resourceFactory;
        this.coreActionsRegistry = coreActionsRegistry;
        this.advancedActionsRegistry = advancedActionsRegistry;
        this.viewWidgetRegistry = viewWidgetRegistry;
        this.explorerWidgetRegistry = explorerWidgetRegistry;
        this.newBusinessObjectVisualAction = newBusinessObjectVisualAction;
        this.newBusinessObjectFromTemplateVisualAction = newBusinessObjectFromTemplateVisualAction;
        this.newMultipleBusinessObjectsVisualAction = newMultipleBusinessObjectsVisualAction;
        this.deleteBusinessObjectVisualAction = deleteBusinessObjectVisualAction;
        this.copyBusinessObjectVisualAction = copyBusinessObjectVisualAction;
        this.moveBusinessObjectVisualAction = moveBusinessObjectVisualAction;
        this.windowMoreInformation = showMoreInformationAction;
        this.newFavoriteVisualAction = newFavoriteVisualAction;
        this.deleteFavoriteVisualAction = deleteFavoriteVisualAction;
        // Attach
        this.newFavoriteVisualAction.registerActionCompletedLister(WindowFavoritesManager.this);
        this.deleteFavoriteVisualAction.registerActionCompletedLister(WindowFavoritesManager.this);
        this.newBusinessObjectFromTemplateVisualAction.registerActionCompletedLister(WindowFavoritesManager.this);
        this.newMultipleBusinessObjectsVisualAction.registerActionCompletedLister(WindowFavoritesManager.this);
        this.newBusinessObjectVisualAction.registerActionCompletedLister(WindowFavoritesManager.this);
        this.deleteBusinessObjectVisualAction.registerActionCompletedLister(WindowFavoritesManager.this);
        this.copyBusinessObjectVisualAction.registerActionCompletedLister(WindowFavoritesManager.this);
        this.moveBusinessObjectVisualAction.registerActionCompletedLister(WindowFavoritesManager.this);
        
        setModal(false);
        setWidth("90%");
        setHeight("90%");
        setDraggable(true);
        setResizable(true);
        setCloseOnEsc(true);
        setContentSizeFull();
        setCloseOnOutsideClick(false);
    }
    
    @Override
    public void open() {
        Scroller scroller = new Scroller();
        scroller.setSizeFull();
              
        lytFolder = new VerticalLayout();
        lytFolder.setWidthFull();
        lytFolder.setSpacing(false);
        lytFolder.setPadding(false);
        lytFolder.setMargin(false);
        
        lytFolderActions = new HorizontalLayout();
        lytFolderActions.setWidthFull();
        lytFolderActions.setHeightFull();
        lytFolderActions.setSpacing(false);
        lytFolderActions.setPadding(false);
        lytFolderActions.setMargin(false);
        
        // Layout for details
        lytDetailsPanel = new VerticalLayout();
        lytDetailsPanel.setWidth("40%");
        lytDetailsPanel.setHeightFull();
        lytDetailsPanel.setSpacing(false);
        lytDetailsPanel.setMargin(false);
        
        lytObjects = new HorizontalLayout();
        lytObjects.setWidthFull();
        lytObjects.setHeightFull();
        lytObjects.setPadding(false);
        lytObjects.setSpacing(false);
        lytObjects.setMargin(false);
       
        lytContent = new VerticalLayout(lytFolder, lytObjects);
        lytContent.setSizeFull();
        lytContent.setHeightFull();
        lytContent.setSpacing(false);
        lytContent.setMargin(false);
        lytContent.setPadding(false);
        lytContent.expand(scroller);
        
        createFilterFavoritesFolder();
        // Action
        Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), (e) ->  super.close());
        btnClose.setClassName("primary-button");
        btnClose.setThemeName("primary");
        btnClose.setWidthFull();
        // Add content to the window
        setHeader(ts.getTranslatedString("module.favorites.name"));
        setContent(lytContent);
        setFooter(btnClose);
        super.open();
    }
    
    private void createFilterFavoritesFolder() {
        try {
            UserProfile user = UI.getCurrent().getSession().getAttribute(Session.class).getUser();
            List<FavoritesFolder> listFolder = aem.getFavoritesFoldersForUser(user.getId());
            // Filter
            ComboBox<FavoritesFolder> cmbFilterFolderName = new ComboBox<>(ts.getTranslatedString("module.favorites.filter"));
            cmbFilterFolderName.setPlaceholder(ts.getTranslatedString("module.favorites.filter.placeholder"));
            cmbFilterFolderName.setAllowCustomValue(false);
            cmbFilterFolderName.setClearButtonVisible(true);
            cmbFilterFolderName.setItems(listFolder);
            cmbFilterFolderName.setItemLabelGenerator(FavoritesFolder::getName);
            cmbFilterFolderName.setWidthFull();
            
            currentFolder = null;
            createFavoritesFolderActions(currentFolder, user);
            // Listener
            cmbFilterFolderName.addValueChangeListener(event -> {
                lytFolderActions.removeAll();
                lytObjects.removeAll();
                if (event.getValue() != null) {
                    currentFolder = event.getValue();
                    buildObjectsGrid(currentFolder, user);
                } else 
                    currentFolder = null;
                createFavoritesFolderActions(currentFolder, user);
            });
            
            HorizontalLayout lytFilter = new HorizontalLayout(cmbFilterFolderName);
            lytFilter.setWidth("50%");
            
            lytFolder.add(lytFilter);
            lytFolder.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, lytFilter);
            lytFolder.add(lytFolderActions);
            lytFolder.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, lytFolderActions);
        } catch (ApplicationObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void createFavoritesFolderActions(FavoritesFolder folder, UserProfile user) {
        Command newFolder = () -> refreshFavoritesFolder();
        ActionButton btnAddFolder = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O)
                , WindowFavoritesManager.this.newFavoriteVisualAction.getModuleAction().getDisplayName());
        btnAddFolder.addClickListener(event -> {
            isFolder = true;
            WindowFavoritesManager.this.newFavoriteVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter<>("user", user),
                    new ModuleActionParameter<>("commandClose", newFolder))).open();
        });
        
        btnEditFolder = new ActionButton(new Icon(VaadinIcon.EDIT), ts.getTranslatedString("module.favorites.actions.favorite-edit-favorite-name"));
        btnEditFolder.addClickListener(event -> new updateFavoriteFolder(folder, user));
        btnEditFolder.setEnabled(false);
        
        Command deleteFolder = () -> {
            refreshFavoritesFolder();
            btnDeleteFolder.setEnabled(false);
            btnEditFolder.setEnabled(false);
        };
        btnDeleteFolder = new ActionButton(new ActionIcon(VaadinIcon.TRASH)
                , WindowFavoritesManager.this.deleteFavoriteVisualAction.getModuleAction().getDisplayName());
        btnDeleteFolder.addClickListener(event -> {
            isFolder = true;
            WindowFavoritesManager.this.deleteFavoriteVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter<>("favoriteFolder", folder),
                    new ModuleActionParameter<>("user", user),
                    new ModuleActionParameter<>("commandClose", deleteFolder))).open();
        });
        btnDeleteFolder.setEnabled(false);
        
        if (folder != null) {
            btnEditFolder.setEnabled(true);
            btnDeleteFolder.setEnabled(true);
        } else {
            btnEditFolder.setEnabled(false);
            btnDeleteFolder.setEnabled(false);
        }
        
        lytFolderActions.add(btnAddFolder, btnEditFolder, btnDeleteFolder);
        lytFolderActions.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
    }
    
    private class updateFavoriteFolder extends Dialog {
        private updateFavoriteFolder(FavoritesFolder folder, UserProfile user) {
            ConfirmDialog wdwUpdateFolder = new ConfirmDialog(ts
                    , String.format(ts.getTranslatedString("module.favorites.label-folder-update"), folder.getName())
                    , ts.getTranslatedString("module.general.messages.save"));
            wdwUpdateFolder.setResizable(true);
            wdwUpdateFolder.setDraggable(true);
            wdwUpdateFolder.setMinWidth("40%");
            
            TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
            txtName.setValue(folder.getName());
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();
            
            wdwUpdateFolder.getBtnConfirm().addClickListener(event -> {
                try {
                    aem.updateFavoritesFolder(folder.getId(), user.getId(), txtName.getValue());
                    if (!txtName.getValue().equals(folder.getName())) 
                        refreshFavoritesFolder();
                    wdwUpdateFolder.close();
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                            ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                            AbstractNotification.NotificationType.INFO, ts).open();
                } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                }
            });
            wdwUpdateFolder.getBtnConfirm().setThemeName("primary");
            wdwUpdateFolder.getBtnConfirm().setClassName("primary-button");

            txtName.addValueChangeListener(event -> {
               wdwUpdateFolder.getBtnConfirm().setEnabled(!txtName.getValue().equals(folder.getName()) && !txtName.getValue().isEmpty());
            });
            
            wdwUpdateFolder.setContent(txtName);
            wdwUpdateFolder.open();
        }
    }
    
    private void buildObjectsGrid(FavoritesFolder folder, UserProfile user) {
        try {
            tblObjects = new TreeGrid<>();
            tblObjects.setPageSize(10);
            tblObjects.setSizeFull();
            tblObjects.setWidthFull();
            tblObjects.setSelectionMode(Grid.SelectionMode.SINGLE);
            List<BusinessObjectLight> folderItems = aem.getObjectsInFavoritesFolder(folder.getId(), user.getId(), LIMIT);
            tblObjects.setDataProvider(buildHierarchicalDataProvider(folderItems));
            // Icon generator
            iconGenerator = new ClassNameIconGenerator(resourceFactory);
            tblObjects.addComponentHierarchyColumn(item -> {
                FormattedObjectDisplayNameSpan spnItemName = new FormattedObjectDisplayNameSpan(
                        item.getObject(), false, false, true, false);
                return new IconNameCellGrid(spnItemName, item.getObject().getClassName(), iconGenerator);
            });
            
            tblObjects.addItemClickListener(event -> {
                isFolder = false;
                currentObject = event.getItem().getObject();
                buildDetailsPanel(currentObject, folder, user);
            });
            VerticalLayout lytObjectsTree = new VerticalLayout(tblObjects);
            lytObjectsTree.setWidth("60%");
            lytObjects.add(lytObjectsTree);
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    public HierarchicalDataProvider buildHierarchicalDataProvider(List<BusinessObjectLight> objects) {
        return new AbstractBackEndHierarchicalDataProvider<FavoritesManagerNode, Void>() {
            @Override
            protected Stream<FavoritesManagerNode> fetchChildrenFromBackEnd(HierarchicalQuery<FavoritesManagerNode, Void> query) {
                FavoritesManagerNode parent = query.getParent();
                if (parent != null) {
                    BusinessObjectLight object = parent.getObject();
                    try {
                        List<BusinessObjectLight> children = bem.getObjectChildren(object.getClassName(), object.getId(), LIMIT);
                        List<FavoritesManagerNode> theChildrenNodes = new ArrayList();
                        children.forEach(child -> {
                            theChildrenNodes.add(new FavoritesManagerNode(child, child.toString()));
                        });
                        return theChildrenNodes.stream();
                    } catch (MetadataObjectNotFoundException ex) {
                        return new ArrayList().stream();
                    } catch (BusinessObjectNotFoundException | InvalidArgumentException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                                AbstractNotification.NotificationType.ERROR, ts).open();
                        return new ArrayList().stream();
                    }
                } else {
                    List<FavoritesManagerNode> objectNodes = new ArrayList();
                    objects.forEach(obj -> objectNodes.add(new FavoritesManagerNode(obj, obj.toString())));
                    return objectNodes.stream();
                }
            }

            @Override
            public int getChildCount(HierarchicalQuery<FavoritesManagerNode, Void> query) {
                FavoritesManagerNode parent = query.getParent();
                if (parent != null) {
                    BusinessObjectLight object = parent.getObject();
                    try {
                        return (int) bem.getObjectChildrenCount(object.getClassName(), object.getId(), null);
                    } catch (InvalidArgumentException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                                ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                        return 0;
                    }
                } else
                    return objects.size();
            }

            @Override
            public boolean hasChildren(FavoritesManagerNode node) {
                return true;
            }
        };
    }
    
    /**
     * Creates the right-most layout with the options for the selected object
     * @param selectedObject the selected object in the nav tree
     */
    private void buildDetailsPanel(BusinessObjectLight selectedObject, FavoritesFolder folder, UserProfile user) {
        try {
            lytDetailsPanel.removeAll();
            if(!selectedObject.getClassName().equals(Constants.DUMMY_ROOT)){
                ObjectOptionsPanel pnlOptions = new ObjectOptionsPanel(selectedObject, 
                        coreActionsRegistry, advancedActionsRegistry, viewWidgetRegistry, explorerWidgetRegistry, mem, aem, bem, ts);
                pnlOptions.setShowViews(false);
                pnlOptions.setShowExplorers(true);
                pnlOptions.setSelectionListener((event) -> {
                    switch (event.getActionCommand()) {
                        case ObjectOptionsPanel.EVENT_ACTION_SELECTION:
                            ModuleActionParameterSet parameters = new ModuleActionParameterSet(
                                    new ModuleActionParameter<>("businessObject", selectedObject));
                            Dialog wdwObjectAction = (Dialog)((AbstractVisualInventoryAction)event.getSource()).getVisualComponent(parameters);
                            wdwObjectAction.open();
                            break;
                        case ObjectOptionsPanel.EVENT_EXPLORER_SELECTION:
                            Dialog wdwExplorer = new Dialog(((AbstractExplorerWidget)event.getSource()).build(selectedObject));
                            wdwExplorer.setHeight("90%");
                            wdwExplorer.setMinWidth("70%");
                            wdwExplorer.setResizable(true);
                            wdwExplorer.setDraggable(true);
                            wdwExplorer.setCloseOnEsc(true);
                            wdwExplorer.open();
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
                            refreshObjectsGrid(folder, user);
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
                // Header
                H4 headerObject = new H4(selectedObject.toString());
                headerObject.setClassName("header-position");
                Icon icon = new Icon(VaadinIcon.INFO_CIRCLE_O);
                icon.setClassName("info-btn");
                Button btnInfo = new Button(icon,
                        e -> {
                            this.windowMoreInformation.getVisualComponent(new ModuleActionParameterSet(
                                    new ModuleActionParameter("object", selectedObject))).open();
                        });
                btnInfo.getElement().setProperty("title", this.windowMoreInformation.getDisplayName());
                btnInfo.setClassName("icon-button");
                HorizontalLayout lytHeader = new HorizontalLayout(headerObject, btnInfo);
                // Add content to layout
                lytDetailsPanel.add(lytHeader, pnlOptions.build(user));
                lytObjects.add(lytDetailsPanel);
            }
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void refreshFavoritesFolder() {
        lytFolder.removeAll();
        lytFolderActions.removeAll();
        lytObjects.removeAll();
        createFilterFavoritesFolder();
    }
    
    private void refreshObjectsGrid(FavoritesFolder folder, UserProfile user) {
        lytObjects.removeAll();
        buildObjectsGrid(folder, user);
    }
}