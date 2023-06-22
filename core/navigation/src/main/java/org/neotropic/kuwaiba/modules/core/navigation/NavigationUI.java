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
package org.neotropic.kuwaiba.modules.core.navigation;

import com.neotropic.flow.component.paper.dialog.PaperDialog;
import com.vaadin.componentfactory.theme.EnhancedDialogVariant;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.ShortcutEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractExplorerWidget;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractObjectRelatedViewWidget;
import org.neotropic.kuwaiba.core.apis.integration.views.ExplorerWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.FilterDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.actions.CopyBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.CopySpecialBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.DefaultDeleteBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.ManageAttachmentsVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.MoveBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.MoveSpecialBusinessObjectActionVisual;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectFromTemplateVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewMultipleBusinessObjectsVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.ReleaseFromVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.ShowMoreInformationAction;
import org.neotropic.kuwaiba.modules.core.navigation.explorers.DialogNavigationSearch;
import static org.neotropic.kuwaiba.modules.core.navigation.explorers.DialogNavigationSearch.MAX_CLASSES_SEARCH_LIMIT;
import static org.neotropic.kuwaiba.modules.core.navigation.explorers.DialogNavigationSearch.MAX_OBJECTS_SEARCH_LIMIT;
import org.neotropic.util.visual.icons.ClassNameIconGenerator;
import org.neotropic.util.visual.grids.BusinessObjectLightGridFilter;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.NavResultGrid;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.nodes.InventoryObjectNode;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.grids.IconNameCellGrid;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.tree.NavTreeGrid;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main entry point to the navigation module.
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Route(value = "navman", layout = NavigationLayout.class)
public class NavigationUI extends VerticalLayout implements 
        ActionCompletedListener, HasDynamicTitle, AbstractUI, PropertySheet.IPropertyValueChangedListener
{
    /**
     * Reference to the action that creates a new Business Object.
     */
    @Autowired
    private NewBusinessObjectVisualAction actNewObj;
    /**
     * Reference to the action that creates a new Business Object from a template.
     */
    @Autowired
    private NewBusinessObjectFromTemplateVisualAction actNewObjFromTemplate;
    
     /**
     * Reference to the action that creates a multiple new Business Object from a pattern.
     */
    @Autowired
    private NewMultipleBusinessObjectsVisualAction actNewMultipleObj;
    /**
     * Reference to the action that deletes a Business Object.
     */
    @Autowired
    private DefaultDeleteBusinessObjectVisualAction actDeleteObj;
    /**
     * Reference to the action that copies a business object to another business object.
     */
    @Autowired
    private CopyBusinessObjectVisualAction actCopyObj;
    /**
     * Reference to the action that copies a special business object to another business object.
     */
    @Autowired
    private CopySpecialBusinessObjectVisualAction actCopySpecialObj;
    /**
     * Reference to the action that moves a business object to another business object.
     */
    @Autowired
    private MoveBusinessObjectVisualAction actMoveObj;
    /**
     * Reference to the action that moves a special business object to another business object.
     */
    @Autowired
    private MoveSpecialBusinessObjectActionVisual actMoveSpecialObj;
    /**
     * Reference to the action that manage Attachments for an object.
     */
    @Autowired
    private ManageAttachmentsVisualAction actManAttachmentsObj;
    /**
     * Reference to the action that release a business object from other business object.
     */
    @Autowired
    private ReleaseFromVisualAction actReleaseFrom;
    /**
     * Reference to the action registry.
     */
    @Autowired
    private CoreActionsRegistry actionRegistry;
    /**
     * Reference to the action registry.
     */
    @Autowired
    private AdvancedActionsRegistry advancedActionsRegistry;
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
     * Factory to build resources from data source.
     */
    @Autowired
    private ResourceFactory resourceFactory;
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
     * The window to show more information about an object.
     */
    @Autowired
    private ShowMoreInformationAction windowMoreInformation;
    /**
     * The actual content. Initially it is just a search box then it can become
     * a page displaying the service/customer details.
     */
    private VerticalLayout lytContent;
    //Gui
    /**
     * Holds both the Locations parents and the device parent
     */
    private HorizontalLayout lytParents;
    /**
     * The bread scrums for location
     */
    private HorizontalLayout lytParentsLocation;
    /**
     * The bread scrums in device
     */
    private HorizontalLayout lytParentsDevice;
    /**
     * Contains the three columns
     */
    private HorizontalLayout lytNav;
    /**
     * RigthColumn
     */
    private VerticalLayout lytLocation;
    /**
     * Center column
     */
    private VerticalLayout lytNetwork;
    /**
     * The right-side panel displaying the property sheet of the selected object plus some other options.
     */
    private VerticalLayout lytDetailsPanel;
    /**
     * Shows the filters combo Box
     */
    private HorizontalLayout lytHeaderNetwork;
    /**
     * contains the nav tree.
     */
    private VerticalLayout lytContentNetwork;
    /**
     * The search bar in the header
     */
    private VerticalLayout lytSearch;
    /**
     * Layout at the bottom of the page to show the views
     */
    private VerticalLayout lytViews;
    /**
     * Header to display a title and a close button for the layout views
     */
    private HorizontalLayout lytHeaderViews;
    /**
     * Button to star exploring from dummy root
     */
    private Button btnExploreFromDummyRoot;
    ///END GUI
    /**
     * The last searched text, it is used to update
     */
    private String lastSearchText;
    /**
     * To keep track of the grids that are been shown after a search
     */
    private HashMap<String, NavResultGrid> classesGrids;
    /**
     * Last set of results after a search, result grouped by class name
     */
    private HashMap<String, List<BusinessObjectLight>> currentSearchedResults;
    /**
     * Filter for the header of nav tree when the show only ports is active
     */
    private ConfigurableFilterDataProvider<InventoryObjectNode, Void, BusinessObjectLightGridFilter> dpConfigurableFilter;
    /**
     * Navigation tree in the central column
     */
    private NavTreeGrid<InventoryObjectNode> navTree;
    /**
     * the component to show the results of a search
     */
    private DialogNavigationSearch searchDialog;
    /**
     * An icon generator for create icons
     */
    private ClassNameIconGenerator iconGenerator;
    /**
     * Pagination controls
     */
    private int currentSkip;
    private int currentLimit;
    private int currentCount;
    private Button btnNext;
    private Button btnBack;
    private Span lblPage;
    private HorizontalLayout lytPagination;

    @Override
    public void onAttach(AttachEvent ev) {
        // To prevent registering the events twice on page reloads.
        this.actNewObjFromTemplate.unregisterListener(this);
        this.actNewMultipleObj.unregisterListener(this);
        this.actNewObj.unregisterListener(this);
        this.actDeleteObj.unregisterListener(this);
        this.actCopyObj.unregisterListener(this);
        this.actMoveObj.unregisterListener(this);
        this.actManAttachmentsObj.unregisterListener(this);
        this.actCopySpecialObj.unregisterListener(this);
        this.actMoveSpecialObj.unregisterListener(this);
        this.actReleaseFrom.unregisterListener(this);
        //Register action listeners.
        this.actNewObjFromTemplate.registerActionCompletedLister(this);
        this.actNewMultipleObj.registerActionCompletedLister(this);
        this.actNewObj.registerActionCompletedLister(this);
        this.actDeleteObj.registerActionCompletedLister(this);
        this.actCopyObj.registerActionCompletedLister(this);
        this.actMoveObj.registerActionCompletedLister(this);
        this.actManAttachmentsObj.registerActionCompletedLister(this);
        this.actCopySpecialObj.registerActionCompletedLister(this);
        this.actMoveSpecialObj.registerActionCompletedLister(this);
        this.actReleaseFrom.registerActionCompletedLister(this);
    }
    
    @Override
    public void onDetach(DetachEvent ev) {
        this.actNewObjFromTemplate.unregisterListener(this);
        this.actNewMultipleObj.unregisterListener(this);
        this.actNewObj.unregisterListener(this);
        this.actDeleteObj.unregisterListener(this);
        this.actCopyObj.unregisterListener(this);
        this.actMoveObj.unregisterListener(this);
        this.actManAttachmentsObj.unregisterListener(this);
        this.actReleaseFrom.unregisterListener(this);
    }

    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            try{
                if(navTree != null) { //we only add/remove nods if the navtree is been shown
                    if(ev.getActionResponse() != null 
                            && ev.getActionResponse().containsKey(NewBusinessObjectVisualAction.PARAM_BUSINESS_OBJECT)
                            && ev.getActionResponse().get(NewBusinessObjectVisualAction.PARAM_BUSINESS_OBJECT) != null) {

                        if (navTree.contains(new InventoryObjectNode((BusinessObjectLight) ev.getActionResponse().get(NewBusinessObjectVisualAction.PARAM_BUSINESS_OBJECT)))) {
                            InventoryObjectNode affectedNode = navTree.findNodeById(((BusinessObjectLight) ev.getActionResponse().get(NewBusinessObjectVisualAction.PARAM_BUSINESS_OBJECT)).getId()).get();

                            if (ev.getActionResponse() != null
                                    && ev.getActionResponse().containsKey(ActionResponse.ActionType.ADD))
                                navTree.update(affectedNode);
                            
                            else if (ev.getActionResponse() != null
                                    && ev.getActionResponse().containsKey(ActionResponse.ActionType.REMOVE)
                                    && navTree.getTreeData().contains(affectedNode))
                                navTreeRemoveNode(affectedNode);
                                                            
                            else if ((ev.getActionResponse() != null
                                    && ev.getActionResponse().containsKey(ActionResponse.ActionType.MOVE))
                                    && navTree.getTreeData().contains(affectedNode))
                                navTree.moveNode(navTree.findNodeById(((BusinessObjectLight) ev.getActionResponse().get(ActionResponse.ActionType.MOVE)).getId()).get(),
                                         affectedNode);
                            
                            else if ((ev.getActionResponse() != null
                                    && ev.getActionResponse().containsKey(ActionResponse.ActionType.COPY))
                                    && navTree.getTreeData().contains(affectedNode))
                                navTree.copyNode(navTree.findNodeById(((BusinessObjectLight) ev.getActionResponse().get(ActionResponse.ActionType.COPY)).getId()).get());
                        } else
                            loadNavigationTree((BusinessObjectLight) ev.getActionResponse().get(NewBusinessObjectVisualAction.PARAM_BUSINESS_OBJECT), null);
                    }
                }
                
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                            AbstractNotification.NotificationType.INFO, ts).open();
            } catch (NoSuchElementException | InvalidArgumentException | 
                    MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
            //We update the UI if we are deleting we must remove the object(s) from the current list of results
            if(ev.getActionResponse().containsKey(ActionResponse.ActionType.REMOVE)){
                BusinessObjectLight obj = (BusinessObjectLight)ev.getActionResponse().get(NewBusinessObjectVisualAction.PARAM_BUSINESS_OBJECT);
                if(classesGrids.get(obj.getClassName()) != null && currentSearchedResults.get(obj.getClassName()).contains(obj)){
                    if(currentSearchedResults.size() == 1){
                        Optional<String> firstKey = currentSearchedResults.keySet().stream().findFirst();
                        if (currentSearchedResults.get(firstKey.get()).size() == 1){
                            lytPagination.setVisible(false);
                            lytParentsLocation.removeAll();
                            lytParentsDevice.removeAll();
                            lytLocation.removeAll();
                            lytHeaderNetwork.removeAll();
                            lytContentNetwork.removeAll();
                            lytDetailsPanel.removeAll();
                        }
                    }
                    else
                        classesGrids.get(obj.getClassName()).getDataProvider().refreshAll();
                
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success")
                                , ts.getTranslatedString("module.navigation.actions.delete-business-object.name-success")
                                , AbstractNotification.NotificationType.INFO, ts).open();
                }
            }
        } else 
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
    }
    
    /**
     * Setups the search bar
     */
    private void setupSearchBar() {
        btnExploreFromDummyRoot = new Button(
                ts.getTranslatedString("module.navigation.actions.explore-from-root"), 
                e -> {
                    lytPagination.setVisible(false);
                    lytParentsLocation.removeAll();
                    lytParentsDevice.removeAll();
                    lytLocation.removeAll();
                    lytHeaderNetwork.removeAll();
                    lytContentNetwork.removeAll();
                    lytDetailsPanel.removeAll();
                    
                    currentSearchedResults = new HashMap<>();
                    List<BusinessObjectLight> list = new ArrayList<>();
                    list.add(new BusinessObjectLight(Constants.DUMMY_ROOT, null, "\\"));
                    currentSearchedResults.put(ts.getTranslatedString("module.general.labels.root"), list);
                    Component results = createGridsClassesResults();
                    lytLocation.add(results);
                    searchDialog.clearSearch();
                }
        );
        
        iconGenerator = new ClassNameIconGenerator(resourceFactory);
        searchDialog = new DialogNavigationSearch(ts, bem, iconGenerator, e -> {
            if(e instanceof String)// No suggestion was chosen
                processSearch((String) e, 0, MAX_CLASSES_SEARCH_LIMIT);
            else { // A single element was selected
                lytPagination.setVisible(false);
                lytParentsLocation.removeAll();
                lytParentsDevice.removeAll();
                lytLocation.removeAll();
                lytHeaderNetwork.removeAll();
                lytContentNetwork.removeAll();
                lytDetailsPanel.removeAll();
                
                currentSearchedResults = new HashMap<>();
                List<BusinessObjectLight> list = new ArrayList<>();
                list.add((BusinessObjectLight) e);
                currentSearchedResults.put(((BusinessObjectLight) e).getClassName(), list);
                Component results = createGridsClassesResults();
                lytLocation.add(results);
            }
            searchDialog.close();
        });

        HorizontalLayout lytSearchBar = new HorizontalLayout(searchDialog, btnExploreFromDummyRoot);
        lytSearchBar.setWidthFull();
        lytSearchBar.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        lytSearchBar.setPadding(false);
        lytSearchBar.setMargin(false);
        lytSearch.add(lytSearchBar);
        lytSearch.add(lytParents);
        lytSearch.add(lytPagination);
    }
     
    /**
     * Creates/updates the localization path, that shows the whole list 
     * of the parents  of the selected object in the tree
     * @param selectedItemParents the selected object in the location tree
     * @param kind the kind of bread crumbs if is location or device
     */
    private Div createParentBreadCrumbs(List<BusinessObjectLight> selectedItemParents, int kind){
        Div divPowerline = new Div();
        divPowerline.setWidthFull();
        divPowerline.setClassName("parents-breadcrumbs");
        
        Collections.reverse(selectedItemParents);
        selectedItemParents.forEach(parent -> {
            Span span = new Span(new Label(parent.getClassName().equals(Constants.DUMMY_ROOT) ? "/" : parent.getName()));
            span.setSizeUndefined();
            span.setTitle(String.format("[%s]",  parent.getClassDisplayName() == null || parent.getClassDisplayName().isEmpty()));
            span.addClassNames("parent", kind == 1 ? "location-parent-color" : "device-parent-color");
            divPowerline.add(span);
        });
        
        return divPowerline;
    }
    
    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.navigation.title");
    }

    @Override
    public void initContent() {
        setPadding(false);
        setMargin(false);
        setSizeFull();

        getUI().ifPresent(ui -> ui.getPage().setTitle(ts.getTranslatedString("module.navigation.title")));
       
        setupLayouts();
        setupSearchBar();
        
        this.navTree = null;
        this.currentSkip = 0;
        this.currentLimit = 10;
        this.currentCount = 0;
        this.currentSearchedResults = new HashMap<>();
        this.classesGrids = new HashMap<>();
                
        initiShortcuts();
    }
     
    /**
     * Initializes the shortcuts handler, Handles rename F2 shortcut
     */
    private void initiShortcuts(){
        ShortcutEventListener listener = event -> {
            if (event.matches(Key.F2)){
                if(navTree != null && navTree.getSelectedItems() != null && !navTree.getSelectedItems().isEmpty()){
                    Optional<InventoryObjectNode> selected = navTree.getSelectedItems().stream().findFirst();
                    selected.ifPresent(s -> shortcutNameEditor(s.getObject()));
                }
                else if(!classesGrids.isEmpty()){
                    Optional<Set> selection = classesGrids.entrySet().stream()
                            .filter(e -> !e.getValue().getSelectedItems().isEmpty())
                            .map(e -> e.getValue().getSelectedItems())
                            .findFirst();

                    selection.ifPresent(s -> {
                        Optional<BusinessObjectLight> first = s.stream().findFirst();
                        first.ifPresent(this::shortcutNameEditor);
                    });
                }
            }
        };
        UI.getCurrent().addShortcutListener(listener, Key.F2);
    }
    
    /**
     * Creates a dialog to edit the node name
     * @param obj selected node
     */
    private void shortcutNameEditor(BusinessObjectLight obj){
        TextField txtEditValue = new TextField(ts.getTranslatedString("module.general.labels.new-name"));
        txtEditValue.setWidthFull();
        txtEditValue.setValue(obj.getName());
        Command cmdRenameObjetc = () -> {
            try {
                HashMap<String, String> attributes = new HashMap<>();
                attributes.put(Constants.PROPERTY_NAME, txtEditValue.getValue());
                bem.updateObject(obj.getClassName(), obj.getId(), attributes);
                refreshGrids(obj.getId(), obj.getClassName(), txtEditValue.getValue());
                txtEditValue.clear();
                buildDetailsPanel(obj);
            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | OperationNotPermittedException | InvalidArgumentException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
            }
        };

        ConfirmDialog dlgRename = new ConfirmDialog(ts
                , txtEditValue
                , ts.getTranslatedString("module.general.labels.rename")
                , cmdRenameObjetc);
        dlgRename.setHeader(ts.getTranslatedString("module.general.labels.rename"));
        dlgRename.setThemeVariants(EnhancedDialogVariant.SIZE_SMALL);
        dlgRename.setWidth(Constants.DEFAULT_SMALL_DIALOG_WIDTH);
        dlgRename.getBtnConfirm().setEnabled(false);

        txtEditValue.setValueChangeMode(ValueChangeMode.EAGER);
        txtEditValue.addValueChangeListener(e ->
            dlgRename.getBtnConfirm().setEnabled(!e.getValue().equals(obj.getName()))
        );

        dlgRename.open();
        txtEditValue.focus();
    }
    
    /**
     * After a search the searched text is process to create a result of 
     * business objects grouped by class name in grids
     * @param searchedText the searched text
     * @param skip the skip for pagination 
     * @param limit the limit for pagination
     */
    private void processSearch(String searchedText
            , int classesSkip, int classesLimit) 
    {
        try {
            classesGrids = new HashMap<>(); //we must clean what its been shown
            int totalCount = 0;
            HashMap<String, List<BusinessObjectLight>> searchResults = new HashMap<>();
            //if the search has changed we must execute the query again
            if(!searchedText.isEmpty() && !searchedText.equals(lastSearchText)){ 
                currentSkip = 0;
                totalCount = bem.getSuggestedObjectsWithFilterGroupedByClassName(null, searchedText, 0, MAX_CLASSES_SEARCH_LIMIT, 0, MAX_OBJECTS_SEARCH_LIMIT).size();
                searchResults = bem.getSuggestedObjectsWithFilterGroupedByClassName(null, searchedText, classesSkip, classesLimit, 0, MAX_OBJECTS_SEARCH_LIMIT);
            }//pagination
            else if(!searchedText.isEmpty() && (classesSkip != currentSkip || currentLimit != classesLimit && searchedText.equals(lastSearchText))){
                searchResults = bem.getSuggestedObjectsWithFilterGroupedByClassName(null, lastSearchText, classesSkip, classesLimit, 0, MAX_OBJECTS_SEARCH_LIMIT);
                currentCount = searchResults.keySet().size();
            }
            //refresh the views if the search changes
            if(!searchResults.isEmpty()){
                if(!currentSearchedResults.equals(searchResults) || currentSearchedResults != null && classesGrids.isEmpty()){
                    currentSearchedResults = searchResults;
                    //Pagination controls
                    if(currentSearchedResults.size() > MAX_CLASSES_SEARCH_LIMIT)
                        lytPagination.setVisible(true);
                    else if(totalCount > 10){
                            if(currentCount < MAX_CLASSES_SEARCH_LIMIT && classesSkip > 0)
                                btnNext.setEnabled(false);
                            else if(classesSkip == 0)
                                btnNext.setEnabled(true);
                    }
                }
                lastSearchText = searchedText;
                currentSkip = classesSkip;
                Component results = createGridsClassesResults();
                lytParentsLocation.removeAll();
                lytParentsDevice.removeAll();
                lytLocation.removeAll();
                lytHeaderNetwork.removeAll();
                lytContentNetwork.removeAll();
                lytDetailsPanel.removeAll();
                lytLocation.add(results);
                int page = (currentSkip + currentLimit)/currentLimit;
                lblPage.setText(Integer.toString(page));
            }
            else if(searchResults.isEmpty() && !searchedText.isEmpty() && !searchedText.equals(lastSearchText)){
                lytPagination.setVisible(false);
                lytParentsLocation.removeAll();
                lytParentsDevice.removeAll();
                lytLocation.removeAll();
                lytHeaderNetwork.removeAll();
                lytContentNetwork.removeAll();
                lytDetailsPanel.removeAll();
                lytLocation.add(new Label(ts.getTranslatedString("module.general.messages.no-search-results")));
            }
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
   
    /**
     * Creates the grids with the results
     * @return an accordion with a grid for every set of classes
     */
    private Component createGridsClassesResults(){
        Accordion acrClasses = new Accordion();
        acrClasses.setWidth("100%");
        navTree = null;
        //we collapse the first grid if there ara more than one
        if(currentSearchedResults.size() > 1)
            acrClasses.close();
        
        for (Map.Entry<String, List<BusinessObjectLight>> entry : currentSearchedResults.entrySet()) {
            String className = entry.getKey();
            
            if(className.equals(ts.getTranslatedString("module.general.labels.root"))){
                Grid<BusinessObjectLight> grid = new Grid<>();
                
                grid.addComponentColumn(item -> {
                    Label lblName = new Label(String.format("/ [%s]", ts.getTranslatedString("module.general.labels.root")));
                    return new IconNameCellGrid(lblName, Constants.DUMMY_ROOT, iconGenerator);
                });
                //object actions
                grid.addComponentColumn(item -> createNodeActions(new InventoryObjectNode(item)))
                        .setFlexGrow(0)
                        .setWidth("90px");
                grid.setItems(entry.getValue());
                grid.addItemClickListener(t -> {
                    try{
                        lytParentsLocation.removeAll();
                        loadNavigationTree(t.getItem(), null);
                        buildDetailsPanel(t.getItem());
                    } catch (InvalidArgumentException | MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                                ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                    }
                });
                grid.setHeightByRows(true);
                return grid;
            }
            else{//not from root
                NavResultGrid<BusinessObjectLight> grid = new NavResultGrid<>(bem,
                        className, lastSearchText, entry.getValue());

                grid.addItemClickListener(t -> {//parents breadcrumbs
                    try{
                        lytParentsLocation.removeAll();
                        List<BusinessObjectLight> selectedItemParents = bem.getParents(t.getItem().getClassName(), t.getItem().getId());
                        lytParentsLocation.add(createParentBreadCrumbs(selectedItemParents, 1));
                        loadNavigationTree(t.getItem(), null);
                        buildDetailsPanel(t.getItem());
                    } catch (InvalidArgumentException | MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                                ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                    }
                });
                //commnet to hide the button actions 
                grid.setFirstColumn(grid.addComponentColumn(item -> {
                        FormattedObjectDisplayNameSpan itemName = new FormattedObjectDisplayNameSpan(item,
                            false, false, true, false);

                        return new IconNameCellGrid(itemName, item.getClassName(), iconGenerator);
                }));
                //object actions
                grid.addComponentColumn(item -> createNodeActions(new InventoryObjectNode(item)))
                        .setFlexGrow(0).setWidth("90px");

                //Sets the max number of items to be rendered on the grid per page
                //only works after the second page, the first page will always loads the after 50 elements by ddefault
                grid.setPageSize(MAX_CLASSES_SEARCH_LIMIT);
                classesGrids.put(className, grid);
                
                if(entry.getValue().size() == 1){
                    grid.setItems(entry.getValue());
                    grid.setHeightByRows(true);
                }
                else{
                    grid.createDataProviderPaginateGrid();
                    grid.createGridFilter();
                }
                
                if(currentSearchedResults.size() == 1) //case for one element selected from search
                    return grid;
                
                acrClasses.add(className, grid);
            }
        }
        return acrClasses;
    }
    
    /**
     * Creates the navigation tree when a item is selected in the grid classes
     * @param obj the selected business object 
     * @return the navigation tree a VaadiTreeGrid 
     * @throws InvalidArgumentException if something is wrong 
     * @throws MetadataObjectNotFoundException if the parent class is not found when the parent's bread crumbs is been created for the selected item in the navigation tree
     * @throws BusinessObjectNotFoundException if the object is not found when the parent's bread crumbs is been created for the selected item in the navigation tree
     */
    private void loadNavigationTree(BusinessObjectLight obj, FilterDefinition filterDefinition) throws InvalidArgumentException, MetadataObjectNotFoundException, BusinessObjectNotFoundException{
        lytContentNetwork.removeAll();
        navTree = new NavTreeGrid<InventoryObjectNode>() {
            @Override
            public List<InventoryObjectNode> fetchData(InventoryObjectNode node) {
                List<InventoryObjectNode> childrenNodes  = new ArrayList<>();
                try{
                    List<BusinessObjectLight> children;
                    if(filterDefinition == null)
                        children = bem.getObjectChildren(node.getObject().getClassName(), node.getObject().getId(), null, -1, -1);
                    else
                        children = filterDefinition.getFilter().run(node.getObject().getId()
                            , node.getObject().getClassName()
                                , null, -1, -1);
                    
                    children.forEach(object -> childrenNodes.add(new InventoryObjectNode(object)));
                } catch (InvalidArgumentException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                        AbstractNotification.NotificationType.ERROR, ts).open();
                }
                return childrenNodes;
            }
        };
        
        navTree.createDataProvider(new InventoryObjectNode(obj));
        navTree.addComponentHierarchyColumn(item -> {
            TextField txtEditor = new TextField();
            txtEditor.setVisible(false);
            
            FormattedObjectDisplayNameSpan spnItemName = new FormattedObjectDisplayNameSpan(item.getObject(),
                    false, false, true, false);
            IconNameCellGrid node = new IconNameCellGrid(spnItemName, item.getObject().getClassName(), iconGenerator);
            return node;
        });
        
        navTree.addComponentColumn(item -> createNodeActions(item))
                .setFlexGrow(0).setWidth("90px")
                .setTextAlign(ColumnTextAlign.CENTER);
        
        navTree.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS
                , GridVariant.LUMO_COMPACT, GridVariant.LUMO_WRAP_CELL_CONTENT);
        
        navTree.setSelectionMode(Grid.SelectionMode.SINGLE);
        navTree.setHeightByRows(true);
        //navTree.setAllRowsVisible(true); //Don't delete this is for the next vaadin version
        navTree.addItemClickListener(e -> {
            if (!navTree.getSelectedItems().isEmpty()) {
                try {
                    lytParentsDevice.removeAll();
                    buildDetailsPanel(e.getItem().getObject());
                    List<BusinessObjectLight> parents = bem.getParentsUntilFirstOfClass(e.getItem().getObject().getClassName(),
                            e.getItem().getObject().getId(), obj.getClassName());
                    lytParentsDevice.add(createParentBreadCrumbs(parents, 2));
                } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | ApplicationObjectNotFoundException | InvalidArgumentException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                }
            }
        });
        loadNavigationTreeFilters(obj, filterDefinition);
        lytContentNetwork.add(navTree);
    }
    /**
     * Loads the filters of the selected object in the header of the nav tree
     * @param obj the selected object in the search results.
     */    
    private void loadNavigationTreeFilters(BusinessObjectLight obj, FilterDefinition filterDefinition){
        lytHeaderNetwork.removeAll();
        List<FilterDefinition> filters = new ArrayList<>();
        FilterDefinition noFilter = new FilterDefinition(-1, ts.getTranslatedString("module.configman.filter.no-filter"), true);
        filters.add(noFilter);

        try {
            HashMap<String, Object> filterAttributes = new  HashMap<>();
            filterAttributes.put(Constants.PROPERTY_ENABLED, true);
            List<FilterDefinition> filterDefinitionsForClass = aem.getFilterDefinitionsForClass(obj.getClassName(), true, false, filterAttributes, -1, -1);
            if(filterDefinitionsForClass != null){
                filterDefinitionsForClass.stream().filter(f -> (f.getFilter() != null)).forEachOrdered(f -> {
                    filters.add(f);
                });
            }
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        }

        
        ComboBox<FilterDefinition> cmbFilters = new ComboBox<>();
        cmbFilters.setWidth("70%");
        cmbFilters.setItems(filters);
        cmbFilters.setValue(filterDefinition == null ? noFilter : filterDefinition);
        cmbFilters.setAllowCustomValue(false);
        cmbFilters.setPlaceholder(ts.getTranslatedString("module.configman.filter.placeholder-select-a-filter"));
        cmbFilters.setRenderer(new ComponentRenderer<>(item -> {
            Label lblNodeName = new Label(item.getName());
            Icon icn = new Icon(VaadinIcon.FILTER);
            icn.setSize("12px");
            HorizontalLayout lytNode = new HorizontalLayout(icn, lblNodeName);
            lytNode.setMargin(false);
            lytNode.setPadding(false);
            lytNode.setDefaultVerticalComponentAlignment(Alignment.END);
            lytNode.setVerticalComponentAlignment(Alignment.CENTER, icn);
            lytNode.getElement().setProperty("title", item.getDescription());
            return lytNode;
        }));
        
        lytHeaderNetwork.add(cmbFilters);
        lytHeaderNetwork.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        
        cmbFilters.addValueChangeListener(e -> {
            try {
                if(e.getValue().getScript() == null || e.getValue().getId() == -1 
                        || (e.getValue().getScript() != null && e.getValue().getScript().isEmpty()))
                    loadNavigationTree(obj, null);
                else if(e.getValue().getFilter() != null)
                    loadNavigationTree(obj, e.getValue());
            
            } catch (InvalidArgumentException | MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
            }
        });
    }
    
    /**
     * Creates the right-most layout with the options for the selected object
     * @param selectedObject the selected object in the nav tree
     */
    private void buildDetailsPanel(BusinessObjectLight selectedObject) {
        try {
            lytDetailsPanel.removeAll();
            if(!selectedObject.getClassName().equals(Constants.DUMMY_ROOT)){
                ObjectOptionsPanel pnlOptions = new ObjectOptionsPanel(selectedObject, 
                        actionRegistry, advancedActionsRegistry, viewWidgetRegistry, explorerWidgetRegistry, mem, aem, bem, ts);
                pnlOptions.setShowViews(true);
                pnlOptions.setShowExplorers(true);
                pnlOptions.setSelectionListener(e -> {
                    try {
                        switch (e.getActionCommand()) {
                            case ObjectOptionsPanel.EVENT_ACTION_SELECTION:
                                ModuleActionParameterSet parameters = new ModuleActionParameterSet(new ModuleActionParameter<>("businessObject", selectedObject));
                                Dialog wdwObjectAction = (Dialog) ((AbstractVisualInventoryAction) e.getSource()).getVisualComponent(parameters);
                                wdwObjectAction.open();
                                break;
                            case ObjectOptionsPanel.EVENT_EXPLORER_SELECTION:
                                Dialog wdwExplorer = new Dialog(((AbstractExplorerWidget) e.getSource()).build(selectedObject));
                                wdwExplorer.setHeight("90%");
                                wdwExplorer.setMinWidth("70%");
                                wdwExplorer.setResizable(true);
                                wdwExplorer.setDraggable(true);
                                wdwExplorer.setCloseOnEsc(true);
                                wdwExplorer.open();
                                break;
                            case ObjectOptionsPanel.EVENT_VIEW_SELECTION:
                                ConfirmDialog wdwView = new ConfirmDialog(ts,ts.getTranslatedString("module.general.messages.close"));
                                wdwView.setHeader(ts.getTranslatedString(String.format(((AbstractObjectRelatedViewWidget) e.getSource()).getTitle(), selectedObject.getName())));
                                wdwView.setContent(((AbstractObjectRelatedViewWidget) e.getSource()).build(selectedObject));
                                wdwView.getBtnConfirm().addClickListener(event -> wdwView.close());
                                wdwView.getBtnCancel().setVisible(false);
                                wdwView.setHeight("87%");
                                wdwView.setMinWidth("70%");
                                wdwView.setResizable(true);
                                wdwView.setDraggable(true);
                                wdwView.setCloseOnEsc(true);
                                wdwView.open();
                                break;
                        }
                    } catch (InventoryException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                                ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
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
                            refreshGrids(selectedObject.getId(), selectedObject.getClassName(), property.getAsString());
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
                // Action go to Dashboard
                Button btnGoToDashboard = new Button(ts.getTranslatedString("module.navigation.widgets.object-dashboard.open-to-dashboard"), ev -> {
                    getUI().ifPresent(ui -> {
                            ui.getSession().setAttribute(BusinessObjectLight.class, selectedObject);
                            ui.getPage().open(RouteConfiguration.forRegistry(VaadinService.getCurrent().getRouter().getRegistry()).getUrl(ObjectDashboard.class), "_blank");
                        });
                });
                btnGoToDashboard.setWidthFull();
                
                // Header
                Label lblTitle = new Label(selectedObject.toString());
                lblTitle.setClassName("dialog-title");

                Button btnInfo = new Button(ts.getTranslatedString("module.navigation.actions.show-more-information-button-name"),
                        e -> {
                            this.windowMoreInformation.getVisualComponent(new ModuleActionParameterSet(
                                    new ModuleActionParameter("object", selectedObject))).open();
                        });
                btnInfo.getElement().setProperty("title", String.format(ts.getTranslatedString("module.navigation.actions.show-more-information"), selectedObject));
                btnInfo.setWidthFull();
                
                HorizontalLayout lytExtraActions = new HorizontalLayout(btnGoToDashboard, btnInfo);
                lytExtraActions.setMaxHeight("60px");
                lytExtraActions.setWidthFull();
                
                // Add content to layout
                lytDetailsPanel.add(lblTitle, lytExtraActions, pnlOptions.build(UI.getCurrent().getSession().getAttribute(Session.class).getUser()));
            }
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    /**
     * Refresh the grids if an attribute is updated
     * @param className the updated class name of the object
     */
    private void refreshGrids(String id, String className, String newName) {
        if(navTree != null){
            for (InventoryObjectNode node : navTree.getAllNodesAsList()) {
                if(node.getObject().getId().equals(id) && navTree.getTreeData().contains(node)){
                    node.getObject().setName(newName);
                    node.setName(newName);
                    navTree.getDataProvider().refreshItem(node);
                    break;
                }
             }
        }

        for (Map.Entry<String, NavResultGrid> entry : classesGrids.entrySet()) {
            if(className.equals(entry.getKey())){
                if(entry.getValue().getDpConfigurableFilter() != null)
                    entry.getValue().getDpConfigurableFilter().refreshAll();
                else{
                    List<BusinessObjectLight> items = (List<BusinessObjectLight>) entry.getValue().getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
                    items.stream().filter(obj -> obj.getId().equals(id)).forEach(obj-> obj.setName(newName));
                    entry.getValue().setItems(items);
                }
                break;
            }
        }
    }
    
    /**
     * Removes selected object from nav tree and cleans the details panel
     * @param affectedNode the selected object to be removed
     */
    private void navTreeRemoveNode(InventoryObjectNode affectedNode) {
        navTree.remove(affectedNode);
        lytDetailsPanel.removeAll();
    }
    
    /**
     * Creates the menu action for a given businessObejctLight
     * @param businessObjectLight the object to apply the actions
     * @return the menu bar with actions
     */
    private Button createNodeActions(InventoryObjectNode node) {
        ActionButton btnMenu = new ActionButton(new Icon(VaadinIcon.ELLIPSIS_DOTS_H));
        
        PaperDialog paperDialog = new PaperDialog();
        paperDialog.getStyle().set("border-top-left-radius", "5px");
        paperDialog.getStyle().set("border-top-right-radius", "5px");
        paperDialog.setWidth("200px");
        paperDialog.setId("menu-" + node.getObject().getId());
        paperDialog.setNoOverlap(true);
        paperDialog.setMargin(false);
        
        FlexLayout lytMenuContent = new FlexLayout();
        lytMenuContent.setSizeFull();
        lytMenuContent.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        lytMenuContent.getStyle().set("margin", "0px"); //NOI18N
        lytMenuContent.getStyle().set("padding", "0px"); //NOI18N
                
        actionRegistry.getActionsForModule(NavigationModule.MODULE_ID)
                .stream().forEach(anAction -> 
        {
            if(anAction != null){
                HorizontalLayout lytMenuElement = new HorizontalLayout();
                lytMenuElement.setClassName("sub-menu-element");
                    
                if(node.getObject().getClassName().equals(Constants.DUMMY_ROOT)){
                    if(!(anAction instanceof DefaultDeleteBusinessObjectVisualAction) &&
                       !(anAction instanceof CopyBusinessObjectVisualAction) &&
                       !(anAction instanceof MoveBusinessObjectVisualAction))
                    {
                        lytMenuElement.add(new Label(anAction.getModuleAction().getDisplayName()));
                        lytMenuContent.add(lytMenuElement);
                        lytMenuElement.addClickListener(e -> {
                            ((Dialog)anAction.getVisualComponent(new ModuleActionParameterSet(
                                new ModuleActionParameter(NewBusinessObjectVisualAction.PARAM_BUSINESS_OBJECT, node.getObject())))).open();
                            paperDialog.close();
                        });
                    }
                }
                else{
                    lytMenuElement.add(new Label(anAction.getModuleAction().getDisplayName()));
                    lytMenuContent.add(lytMenuElement);
                    lytMenuElement.addClickListener(e -> {
                            ((Dialog)anAction.getVisualComponent(new ModuleActionParameterSet(
                                new ModuleActionParameter(NewBusinessObjectVisualAction.PARAM_BUSINESS_OBJECT, node.getObject())))).open();
                            paperDialog.close();
                        });
                }
            }
        });
        
        paperDialog.add(lytMenuContent);
        btnMenu.addClickListener(e -> paperDialog.open("menu-" + node.getObject().getId(), btnMenu, false));
        lytLocation.add(paperDialog);
        
        return btnMenu;
    }
    
    /**
     * setUp the layouts 
     * 
     * lytContent:
     * -----------------------------------------------------
     *                          lytSearch
     * lytParents: [ lytParentsLocation | lytParentsDevice ]
     * lytPagination
     * -----------------------------------------------------
     * lytNav:            |              |
     *      lytLocation   |  lytNetwork  |   lytDetails
     *                    |              |
     * -----------------------------------------------------
     * lytViews
     */
    private void setupLayouts() {
        //in the header after the nav layout
        if(lytSearch == null){
            lytSearch = new VerticalLayout();
            lytSearch.setSpacing(true);
            lytSearch.setMargin(false);
            lytSearch.setPadding(false);
            lytSearch.setBoxSizing(BoxSizing.BORDER_BOX);
            lytSearch.setId("nav-search-component");
            lytSearch.setWidth("100%");
            lytSearch.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
            
            btnNext = new Button(">");
            btnBack = new Button("<");
            lblPage = new Span("1");
            
            btnNext.addClickListener(e -> {
                int nextSkip = currentSkip + currentLimit;
                
                btnBack.setEnabled(true);
                processSearch(lastSearchText, nextSkip, currentLimit);
            });

            btnBack.addClickListener(e -> {
                int backSkip = currentSkip - currentLimit;
                if(backSkip < 0)
                    btnBack.setEnabled(false);
                else{
                    processSearch(lastSearchText, backSkip, currentLimit);
                    btnNext.setEnabled(currentCount == currentLimit);
                }
            });
            
            btnBack.setEnabled(false);
            lytPagination = new HorizontalLayout(btnBack, lblPage, btnNext);
            lytPagination.setWidth("200px");
            lytPagination.setDefaultVerticalComponentAlignment(Alignment.CENTER);
            lytPagination.setVisible(false);
        }
        //Main column that contains the three columns
        if(lytNav == null){
            lytNav = new HorizontalLayout();
            lytNav.setSpacing(false);
            lytNav.setMargin(false);
            lytNav.setPadding(false);
            lytNav.setSizeFull();
        }
        //The rigth colum
        if(lytLocation == null){
            lytLocation = new VerticalLayout();
            lytLocation.setWidth("31%");
        }
        //the first row of the rigth column
        if(lytParents == null){
            lytParentsLocation = new HorizontalLayout();
            lytParentsLocation.setSpacing(false);
            lytParentsLocation.setMargin(false);
            lytParentsLocation.setPadding(false);
            
            lytParentsDevice= new HorizontalLayout();
            lytParentsDevice.setSpacing(false);
            lytParentsDevice.setMargin(false);
            lytParentsDevice.setPadding(false);
            
            lytParents = new HorizontalLayout(lytParentsLocation, lytParentsDevice);
            lytParents.setSpacing(true);
            lytParents.setMargin(false);
            lytParents.setPadding(false);
            lytParents.setMinHeight("15px");
            lytParents.setWidth("100%");
        }
        //Center column
        
        if(lytNetwork == null){
            this.lytHeaderNetwork = new HorizontalLayout();
            this.lytHeaderNetwork.setWidthFull();
            this.lytHeaderNetwork.setSpacing(true);
            this.lytHeaderNetwork.setMargin(false);
            this.lytHeaderNetwork.setPadding(false);

            this.lytContentNetwork = new VerticalLayout();
            this.lytContentNetwork.setSpacing(true);
            this.lytContentNetwork.setMargin(false);
            this.lytContentNetwork.setPadding(false);
            
            this.lytNetwork = new VerticalLayout(lytHeaderNetwork, lytContentNetwork);
            this.lytNetwork.setWidth("36%");
        }
        //Right column
        if(lytDetailsPanel == null){
            this.lytDetailsPanel = new VerticalLayout();
            this.lytDetailsPanel.setSpacing(false);
            this.lytDetailsPanel.setMargin(false);
            this.lytDetailsPanel.setPadding(false);
            this.lytDetailsPanel.setWidth("31%");
        }
        //Bottom view
        if(lytHeaderViews == null){
            lytHeaderViews = new HorizontalLayout();
            lytHeaderViews.setSpacing(true);
            lytHeaderViews.setMargin(false);
            lytHeaderViews.setPadding(false);
            lytHeaderViews.setMinHeight("15px");
            lytHeaderViews.setWidth("100%");
        }
        if(lytViews == null){
            this.lytViews = new VerticalLayout();
            this.lytViews.setSpacing(false);
            this.lytViews.setMargin(false);
            this.lytViews.setPadding(false);
            this.lytViews.setWidth("100%");
        }
        //the main content 
        this.lytContent = new VerticalLayout();
        this.lytContent.setSizeFull();
        this.lytContent.setSpacing(false);
        this.lytContent.setMargin(false);
        this.lytContent.setPadding(false);

        //this are the three parts in the may layout
        lytNav.add(lytLocation);
        lytNav.add(lytNetwork);
        lytNav.add(lytDetailsPanel);
        
        lytContent.add(lytSearch, lytNav, lytViews);
        add(lytContent);
    }

    @Override
    public void updatePropertyChanged(AbstractProperty<? extends Object> property) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}