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
package org.neotropic.kuwaiba.modules.optional.serviceman;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractExplorerWidget;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ExplorerWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
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
import org.neotropic.kuwaiba.modules.core.navigation.ObjectDashboard;
import org.neotropic.kuwaiba.modules.core.navigation.ObjectOptionsPanel;
import org.neotropic.kuwaiba.modules.core.navigation.actions.ShowMoreInformationAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.DeleteCustomerPoolVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.DeleteCustomerVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.DeleteServicePoolVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.DeleteServiceVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.NewCustomerPoolVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.NewCustomerVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.NewServicePoolVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.NewServiceVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.RelateObjectToServiceVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.ReleaseObjectFromServiceVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.components.ServiceManTreeNode;
import org.neotropic.kuwaiba.modules.optional.serviceman.explorers.DialogServiceManagerSearch;
import org.neotropic.kuwaiba.modules.optional.serviceman.visual.IconLabelCellGrid;
import org.neotropic.kuwaiba.modules.optional.serviceman.widgets.CustomerDashboard;
import org.neotropic.kuwaiba.modules.optional.serviceman.widgets.ServiceDashboardFactory;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.icons.ClassNameIconGenerator;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.tree.NavTreeGrid;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the service manager module. This class manages how the pages
 * corresponding to different functionalities are presented in a single place.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route(value = "serviceman", layout = ServiceManagerLayout.class)
public class ServiceManagerUI extends VerticalLayout implements ActionCompletedListener
        , PropertySheet.IPropertyValueChangedListener, HasDynamicTitle, AbstractUI { 
    /**
     * Reference to the Service Manager Service
     */
    @Autowired
    private ServiceManagerService sms;
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
     * Reference to the Service Dashboard Factory
     */
    @Autowired
    private ServiceDashboardFactory serviceDashboardFactory;
    /**
     * Factory to build resources from data source.
     */
    @Autowired
    private ResourceFactory resourceFactory;
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
     * Reference to the action that creates customer pools.
     */
    @Autowired
    private NewCustomerPoolVisualAction newCustomerPoolVisualAction;
    /**
     * Reference to the action that creates customers.
     */
    @Autowired
    private NewCustomerVisualAction newCustomerVisualAction;
    /**
     * Reference to the action that creates service pools.
     */
    @Autowired
    private NewServicePoolVisualAction newServicePoolVisualAction;
    /**
     * Reference to the action that creates services.
     */
    @Autowired
    private NewServiceVisualAction newServiceVisualAction;
    /**
     * Reference to the action that deletes customer pools.
     */
    @Autowired
    private DeleteCustomerPoolVisualAction deleteCustomerPoolVisualAction;
    /**
     * Reference to the action that deletes customers.
     */
    @Autowired
    private DeleteCustomerVisualAction deleteCustomerVisualAction;
    /**
     * Reference to the action that deletes service pools.
     */
    @Autowired
    private DeleteServicePoolVisualAction deleteServicePoolVisualAction;
    /**
     * Reference to the action that deletes services.
     */
    @Autowired
    private DeleteServiceVisualAction deleteServiceVisualAction;
    /**
     * Reference to the action that releases business objects from services.
     */
    @Autowired
    private ReleaseObjectFromServiceVisualAction releaseObjectFromServiceVisualAction;
    /**
     * Reference to the action that releases business objects from services.
     */
    @Autowired
    private RelateObjectToServiceVisualAction relateObjectToServiceVisualAction;
    /**
     * Parameter for customer pool
     */
    public static final String PARAMETER_CUSTOMER_POOL = "customerPool";
    /**
     * Parameter for customer
     */
    public static final String PARAMETER_CUSTOMER = "customer";
    /**
     * Parameter for service pool
     */
    public static final String PARAMETER_SERVICE_POOL = "servicePool";
    /**
     * Parameter for service
     */
    public static final String PARAMETER_SERVICE = "service";
    /**
     * Parameter business object.
     */
    public static String PARAMETER_BUSINESS_OBJECT = "businessObject";
    /**
     * display Number of classes for page
     */
    private static final int RESULTS_CLASSES_PER_PAGE = 5;
    /**
     * number of object per class
     */
    private static final int RESULTS_OBJECTS_PER_CLASS = 8;
    // --> Init GUI
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
    private HorizontalLayout lytMainNav;
    /**
     * Left Column
     */
    private VerticalLayout lytLeftSide;
    private HorizontalLayout lytLeftControls; 
    /**
     * Center column
     */
    private VerticalLayout lytCenter;
    /**
     * The right-side panel displaying the property sheet of the selected object
     * plus some other options.
     */
    private VerticalLayout lytDetailsPanel;
    /**
     * Holds the name of the selected object and the option: show only ports
     */
    private HorizontalLayout lytHeaderNetwork;
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
    /**
     * Button to add a customer pool
     */
    private ActionButton btnAddCustomerPool;
    // --> End GUI
    /**
     * An icon generator for create icons
     */
    private ClassNameIconGenerator iconGenerator;
    /**
     * Sub-header with shortcut to common actions such as creating a service or
     * a customer.
     */
    private HorizontalLayout lytQuickActions;
    /**
     * The actual content. Initially it is just a search box then it can become
     * a page displaying the service/customer details.
     */
    private VerticalLayout lytContent;
    /**
     * the component to show the results of a search
     */
    private DialogServiceManagerSearch searchDialog;
    /**
     * left grid with search results
     */
    private Grid<ServiceManTreeNode> gridLeftNav;
    /**
     * Navigation tree in the central column
     */
    private NavTreeGrid<ServiceManTreeNode> navTree;
    /**
     * to keep record of the item who launch an action
     */
    private List<ServiceManTreeNode> actionAffectedNode;
    /**
     * Pool property sheet
     */
    private PropertySheet propertySheetPool;
    /**
     * node selected in any grid
     */
    private ServiceManTreeNode nodeSelected;
    /**
     * Object to save the current service
     */
    private BusinessObjectLight currentService;

    @Override
    public void onDetach(DetachEvent ev) {
        this.newCustomerPoolVisualAction.unregisterListener(this);
        this.newCustomerVisualAction.unregisterListener(this);
        this.newServicePoolVisualAction.unregisterListener(this);
        this.newServiceVisualAction.unregisterListener(this);
        // delete action
        this.deleteCustomerPoolVisualAction.unregisterListener(this);
        this.deleteCustomerVisualAction.unregisterListener(this);
        this.deleteServicePoolVisualAction.unregisterListener(this);
        this.deleteServiceVisualAction.unregisterListener(this);
        // relate/release action
        this.relateObjectToServiceVisualAction.unregisterListener(this);
        this.releaseObjectFromServiceVisualAction.unregisterListener(this);
    }

    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            if (ev.getActionResponse() != null && (ev.getActionResponse().containsKey(ActionResponse.ActionType.ADD))) {
                if (!actionAffectedNode.isEmpty() && actionAffectedNode.get(0) != null) {
                    if (navTree != null) {
                        if (((String) ev.getActionResponse().get(ActionResponse.ActionType.ADD)).isEmpty())
                            navTree.update(actionAffectedNode.get(0));
                        else {
                            if (navTree.getTreeData().contains(actionAffectedNode.get(0)))
                                navTree.getDataProvider().refreshItem(actionAffectedNode.get(0), false);
                        }
                    }
                }
            } else if (ev.getActionResponse() != null && ev.getActionResponse().containsKey(ActionResponse.ActionType.REMOVE)) {
                if(!actionAffectedNode.isEmpty() && actionAffectedNode.get(0) != null && navTree.getTreeData().contains(actionAffectedNode.get(0)))
                    navTree.remove(actionAffectedNode.get(0));
                
                //we must deselect the current selected node if the affecetated node is the same
                if (nodeSelected != null && actionAffectedNode != null && actionAffectedNode.get(0) != null) {
                    if (actionAffectedNode.get(0).getId().equals(nodeSelected.getId()))
                        nodeSelected = null;
                }

                lytDetailsPanel.removeAll();
            }
            actionAffectedNode.set(0, null);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                            AbstractNotification.NotificationType.INFO, ts).open();
        } else 
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                AbstractNotification.NotificationType.ERROR, ts).open();
    }

    public void replaceContent(Component newContent) {
        this.lytContent.removeAll();
        this.lytContent.add(newContent);
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.serviceman.title");
    }

    @Override
    public void initContent() {
        setPadding(false);
        setMargin(false);
        setSizeFull();
        
        setupLayouts();
        setupSearchBar();
        
        // Action affected node
        this.actionAffectedNode = new ArrayList<>();
        this.actionAffectedNode.add(null);
        // Register action listeners
        this.newCustomerPoolVisualAction.registerActionCompletedLister(this);
        this.newCustomerVisualAction.registerActionCompletedLister(this);
        this.newServicePoolVisualAction.registerActionCompletedLister(this);
        this.newServiceVisualAction.registerActionCompletedLister(this);
        // delete action
        this.deleteCustomerPoolVisualAction.registerActionCompletedLister(this);
        this.deleteCustomerVisualAction.registerActionCompletedLister(this);
        this.deleteServicePoolVisualAction.registerActionCompletedLister(this);
        this.deleteServiceVisualAction.registerActionCompletedLister(this);
        // relate/release action
        this.relateObjectToServiceVisualAction.registerActionCompletedLister(this);
        this.releaseObjectFromServiceVisualAction.registerActionCompletedLister(this);
    }

    /**
     * Functional interface intended to be used to create the content that will
     * be placed in the page when a search result is clicked.
     * @param <T> The type of the search result.
     */
    public interface SearchResultCallback<T> {

        /**
         * Given a search result, builds content to be displayed in the page.
         * @param searchResult The search result to be expanded.
         * @return The visual component that will show the detailed information
         * about the search result.
         */
        public Component buildSearchResultDetailsPage(T searchResult);
    }

    public class CustomerSearchResultCallback implements SearchResultCallback<BusinessObjectLight> {

        @Override
        public Component buildSearchResultDetailsPage(BusinessObjectLight searchResult) {
            return new CustomerDashboard(searchResult);
        }
    }

    public class ServiceSearchResultCallback implements SearchResultCallback<BusinessObjectLight> {

        @Override
        public Component buildSearchResultDetailsPage(BusinessObjectLight searchResult) {
            return serviceDashboardFactory.build(searchResult);
        }
    }

    /**
     * setUp the layouts
     * lytContent: -----------------------------------------------------
     * lytSearch lytParents: | lytParentsLocation | lytParentsDevice|
     * lytPagination -----------------------------------------------------
     * lytNav: | | lytLocation | lytNetwork | lytDetails | |
     * ----------------------------------------------------- lytViews
     */
    private void setupLayouts() {
        //in the header after the nav layout
        if (lytSearch == null) {
            lytSearch = new VerticalLayout();
            lytSearch.setSpacing(false);
            lytSearch.setMargin(false);
            lytSearch.setPadding(false);
            lytSearch.setBoxSizing(BoxSizing.BORDER_BOX);
            lytSearch.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
            lytSearch.setId("nav-search-component");
            lytSearch.setWidthFull();
        }
        //Main column that contains the three columns
        if (lytMainNav == null) {
            lytMainNav = new HorizontalLayout();
            lytMainNav.setId("nav-main-lyt");
            lytMainNav.setSpacing(false);
            lytMainNav.setMargin(false);
            lytMainNav.setPadding(false);
            lytMainNav.setSizeFull();
        }
        //The rigth colum
        if (lytLeftSide == null) {
            lytLeftSide = new VerticalLayout();
            lytLeftSide.setId("left-main-lyt");
            lytLeftSide.setSpacing(false);
            lytLeftSide.setMargin(false);
            lytLeftSide.setPadding(false);
            lytLeftSide.setVisible(false);
            lytLeftSide.setClassName("serviceman-left-main-lyt");
        }
        //the first row of the rigth column
        if (lytParents == null) {
            lytParentsLocation = new HorizontalLayout();
            lytParentsLocation.setSpacing(false);
            lytParentsLocation.setMargin(false);
            lytParentsLocation.setPadding(false);

            lytParentsDevice = new HorizontalLayout();
            lytParentsDevice.setSpacing(false);
            lytParentsDevice.setMargin(false);
            lytParentsDevice.setPadding(false);

            lytParents = new HorizontalLayout(lytParentsLocation, lytParentsDevice);
            lytParents.setSpacing(true);
            lytParents.setMargin(false);
            lytParents.setPadding(false);
            lytParents.setWidthFull();
        }
        //Center column
        if (lytCenter == null) {
            this.lytCenter = new VerticalLayout();
            this.lytCenter.setId("center-main-lyt");
            this.lytHeaderNetwork = new HorizontalLayout();
            this.lytHeaderNetwork.setSpacing(false);
            this.lytHeaderNetwork.setMargin(false);
            this.lytHeaderNetwork.setPadding(false);
        }
        //Right column
        if (lytDetailsPanel == null) {
            this.lytDetailsPanel = new VerticalLayout();
            this.lytDetailsPanel.setSpacing(false);
            this.lytDetailsPanel.setMargin(false);
            this.lytDetailsPanel.setPadding(false);
            this.lytDetailsPanel.setClassName("serviceman-property-sheet-details-panel");
        }
        //Bottom view
        if (lytHeaderViews == null) {
            lytHeaderViews = new HorizontalLayout();
            lytHeaderViews.setSpacing(true);
            lytHeaderViews.setMargin(false);
            lytHeaderViews.setPadding(false);
            lytHeaderViews.setWidthFull();
        }
        if (lytViews == null) {
            this.lytViews = new VerticalLayout();
            this.lytViews.setSpacing(false);
            this.lytViews.setMargin(false);
            this.lytViews.setPadding(false);
            this.lytViews.setWidthFull();
        }
        if (lytLeftControls == null)
            lytLeftControls = new HorizontalLayout();
            
        //the main content 
        this.lytContent = new VerticalLayout();
        this.lytContent.setSizeFull();
        this.lytContent.setSpacing(false);
        this.lytContent.setMargin(false);
        this.lytContent.setPadding(false);

        //this are the three parts in the may layout
        lytMainNav.addAndExpand(lytLeftSide, lytCenter, lytDetailsPanel);
        
        lytContent.add(lytSearch, lytMainNav, lytViews);
        add(lytContent);
    }

    /**
     * Setups the search bar
     */
    private void setupSearchBar() {
        btnExploreFromDummyRoot = new Button(ts.getTranslatedString("module.serviceman.actions.explore-from-root"));
        btnExploreFromDummyRoot.addClickListener(event -> {
            List<ServiceManTreeNode> items = new ArrayList<>();
            clearElements();
            try {
                List<InventoryObjectPool> customerPools = sms.getCustomerPools();
                items = customerPools.stream().map(item -> {
                    ServiceManTreeNode firstNode = new ServiceManTreeNode(item);
                    firstNode.setPool(true);
                    loadItemTreeTags(firstNode);
                    return firstNode;
                }).collect(Collectors.toList());
            } catch (InvalidArgumentException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
            }
                        
            buildLeftNavGrid();
            createLeftGridAction();
            gridLeftNav.setItems(items);
            lytLeftSide.addComponentAsFirst(createParentBreadCrumbs(items, 1));
            searchDialog.clearSearch();
            lytLeftControls.setVisible(true);
        });

        iconGenerator = new ClassNameIconGenerator(resourceFactory);
        searchDialog = new DialogServiceManagerSearch(ts, bem, iconGenerator, e -> {
            if (e instanceof String) {// No suggestion was chosen
                processSearch((String) e, 0, RESULTS_CLASSES_PER_PAGE);
            } else { // A single element was selected
                clearElements();
                List<ServiceManTreeNode> list = new ArrayList<>();
                if (e != null && e instanceof BusinessObjectLight) {
                    ServiceManTreeNode firstNode = new ServiceManTreeNode((BusinessObjectLight) e);
                    loadItemTreeTags(firstNode);
                    list.add(firstNode);
                    lytLeftControls.setVisible(false);
                } else {
                    ServiceManTreeNode firstNode = new ServiceManTreeNode((InventoryObjectPool) e);
                    loadItemTreeTags(firstNode);
                    list.add(firstNode);
                    if(firstNode.isService())
                        lytLeftControls.setVisible(false);
                    else if (firstNode.isCustomer())
                        lytLeftControls.setVisible(true);
                }

                buildLeftNavGrid();
                gridLeftNav.setItems(list);
                lytLeftSide.addComponentAsFirst(createParentBreadCrumbs(list, 1));
            }
            searchDialog.close();
        });

        HorizontalLayout lytSearchBar = new HorizontalLayout(searchDialog, btnExploreFromDummyRoot);
        lytSearchBar.setWidthFull();
        lytSearchBar.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        lytSearchBar.setPadding(false);
        lytSearchBar.setMargin(false);

        lytSearch.add(lytSearchBar);
        lytSearch.setHorizontalComponentAlignment(FlexComponent.Alignment.END, lytSearchBar);
        lytSearch.add(lytParents);
    }

    /**
     * After a search the searched text is process to create a result of
     * business objects grouped by class name in grids
     *
     * @param searchedText the searched text
     * @param skip the skip for pagination
     * @param limit the limit for pagination
     */
    private void processSearch(String searchedText, int skip, int limit) {
        try {
            List<BusinessObjectLight> searchBObjestResult = null;
            List<InventoryObjectPool> searchPoolResult = null;
            HashMap<String, List<InventoryObjectPool>> suggestedPoolsByName;
            //if the search has changed we must execute the query again
            if (!searchedText.isEmpty()) {
                searchBObjestResult = bem.getSuggestedObjectsWithFilter(searchedText, skip, limit,
                        Constants.CLASS_GENERICSERVICE,
                        Constants.CLASS_GENERICCUSTOMER
                );
                suggestedPoolsByName = bem.getSuggestedPoolsByName(
                        Arrays.asList(Constants.CLASS_GENERICSERVICE
                                        , Constants.CLASS_GENERICCUSTOMER)
                                , searchedText, skip, limit, 0, RESULTS_OBJECTS_PER_CLASS);
            }
            lytLeftSide.removeAll();
            if (searchBObjestResult == null || searchBObjestResult.isEmpty())
                lytLeftSide.add(new Label(ts.getTranslatedString("module.general.messages.no-search-results")));
            else {
                buildLeftNavGrid();
                List<ServiceManTreeNode> items = searchBObjestResult.stream().map(item -> {
                    ServiceManTreeNode firstNode = new ServiceManTreeNode(item);
                    loadItemTreeTags(firstNode);
                    return firstNode;

                }).collect(Collectors.toList());
                gridLeftNav.setItems(items);
                lytLeftSide.addComponentAsFirst(createParentBreadCrumbs(items, 1));                
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
    private void buildLeftNavGrid() {                
        gridLeftNav = new Grid<>();        
        gridLeftNav.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        gridLeftNav.setSelectionMode(Grid.SelectionMode.SINGLE);
        gridLeftNav.setHeightByRows(true);
        
        gridLeftNav.addComponentColumn(obj
                -> new IconLabelCellGrid((ServiceManTreeNode) obj,
                        ((ServiceManTreeNode) obj).isPool(),
                        ((ServiceManTreeNode) obj).isSelected(),
                        iconGenerator)
        );
            
        gridLeftNav.addComponentColumn(obj -> createActions(obj))
                .setTextAlign(ColumnTextAlign.END).setFlexGrow(0);
        
        //object actions
        gridLeftNav.addItemClickListener(event -> {
            nodeSelected = event.getItem();
            lytParentsLocation.removeAll();
            loadTreeGrid(nodeSelected);
            if (nodeSelected.isPool()) {
                event.getItem().setSelected(true);
                gridLeftNav.getDataProvider().refreshItem(nodeSelected);
            }
            buildDetailsPanel(event.getItem());
        });
                
        btnAddCustomerPool = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O),
                this.newCustomerPoolVisualAction.getModuleAction().getDisplayName());
        btnAddCustomerPool.setHeight("20px");
        btnAddCustomerPool.setWidth("20px");
        
        // Layout for action buttons
        HorizontalLayout lytLeftActionButtons = new HorizontalLayout();
        lytLeftActionButtons.setClassName("button-container");
        lytLeftActionButtons.setPadding(false);
        lytLeftActionButtons.setMargin(false);
        lytLeftActionButtons.setSpacing(false);
        lytLeftActionButtons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        lytLeftActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        lytLeftActionButtons.add(btnAddCustomerPool);
        
        Label lblCustomerPool = new Label(ts.getTranslatedString("module.serviceman.header"));
        lblCustomerPool.setClassName("dialog-title");
        HorizontalLayout lytFilterName = new HorizontalLayout(lblCustomerPool);
        lytFilterName.setMargin(false);
        lytFilterName.setPadding(false);
        lytFilterName.setWidth("65%");

        lytLeftControls.removeAll();
        lytLeftControls.setVisible(false);
        lytLeftControls.setPadding(false);
        lytLeftControls.setMargin(false);
        lytLeftControls.setSpacing(false);
        lytLeftControls.setWidthFull();
        lytLeftControls.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytLeftControls.add(lytFilterName, lytLeftActionButtons);

        VerticalLayout lytGridHeader = new VerticalLayout();
        lytGridHeader.setClassName("header-script-control");
        lytGridHeader.setPadding(false);
        lytGridHeader.setMargin(false);
        lytGridHeader.setSpacing(false);
        lytGridHeader.setSpacing(false);
        lytGridHeader.add(lytLeftControls);
        
        //left tree grid
        VerticalLayout lytElementsInGrid = new VerticalLayout();
        lytElementsInGrid.setHeightFull();
        lytElementsInGrid.setClassName("serviceman-left-grid-lyt");
        lytElementsInGrid.setMargin(false);
        lytElementsInGrid.setSpacing(false);
        lytElementsInGrid.setPadding(false);
        lytElementsInGrid.add(gridLeftNav);
        lytLeftSide.add(lytGridHeader, lytElementsInGrid);
        lytLeftSide.setVisible(true);
    }

    /**
     * Creates/updates the localization path, that shows the whole list 
     * of the parents  of the selected object in the tree
     * @param selectedItemParents the selected object in the location tree
     * @param kind the kind of bread crumbs if is location or device
     */
    private Div createParentBreadCrumbs(List<ServiceManTreeNode> selectedItemParents, int kind) {
        Div divPowerline = new Div();
        divPowerline.setWidthFull();
        divPowerline.setClassName("serviceman-parents-breadcrumbs");

        Collections.reverse(selectedItemParents);
        selectedItemParents.forEach(parent -> {
            if (parent.getObject() instanceof BusinessObjectLight) {
                try {
                    Span span = new Span(new Label(parent.getClassName().equals(Constants.DUMMY_ROOT) ? "/" : parent.getName()));
                    span.setSizeUndefined();
                    span.setTitle(String.format("[%s]", mem.getClass(parent.getClassName()).toString()));
                    span.addClassNames("parent", kind == 1 ? "location-parent-color" : "device-parent-color");
                    divPowerline.add(span);
                } catch (MetadataObjectNotFoundException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error")
                            , ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                }
            }
        });

        return divPowerline;
    }

    /**
     * Navigation Tree providers 
     * Creates the data provider from a given class to filter
     * ------------------------------------ hierarchy ------------------------------------
     * |<InventoryObjectPool> getCustomerPools (Customers Pools) pool class: GenericCustomer
     * |_ <BusinessObjectLight> getCustomersInPool (customers) object GenericCustomer subclasses
     *   |_<InventoryObjectPool> getServicePoolsInCostumer (service Pool) pool class: GenericServices
     *     |_<BusinessObjectLight> getServicesInPool (services) objet GenericServices subclasses
     *       |_<BusinessObjectLight> getObjectsRelatedToService (service related resources "uses") objet
     *
     * @param aNode the selected item
     */
    private void loadTreeGrid(ServiceManTreeNode aNode) {
        lytCenter.removeAll();
        navTree = new NavTreeGrid<ServiceManTreeNode>() {
            @Override
            public List<ServiceManTreeNode> fetchData(ServiceManTreeNode node) {
                //fetch children
                List<ServiceManTreeNode> childrenNode = new ArrayList<>();
                try {
                    if (node.isCustomer()) {
                        if (node.isPool()) {
                            List<BusinessObjectLight> customers = sms.getCustomersInPool(node.getId(), null, 0, 50);
                            
                            customers.forEach(customer -> {
                                ServiceManTreeNode object = new ServiceManTreeNode(customer);
                                loadItemTreeTags(object);
                                childrenNode.add(object);
                            });
                        } else {
                            List<InventoryObjectPool> servicePools = sms.getServicePoolsInCostumer(node.getClassName()
                                    , node.getId(), Constants.CLASS_GENERICSERVICE);
                            
                            servicePools.forEach(servicePool -> {
                                ServiceManTreeNode pool = new ServiceManTreeNode(servicePool);
                                loadItemTreeTags(pool);
                                childrenNode.add(pool);
                            });
                        }
                    } else if (node.isService()) {
                        if (node.isPool()) {
                            List<BusinessObjectLight> services = sms.getServicesInPool(node.getId(), null, 0, 50);
                            
                            services.forEach(service -> {
                                ServiceManTreeNode object = new ServiceManTreeNode(service);
                                loadItemTreeTags(object);
                                childrenNode.add(object);
                            });
                        } else {
                            List<BusinessObjectLight> objects = sms.getObjectsRelatedToService(node.getClassName(), node.getId());

                            objects.forEach(object -> {
                                ServiceManTreeNode item = new ServiceManTreeNode(object);
                                loadItemTreeTags(item);
                                childrenNode.add(item);
                            });
                        }
                    }
                } catch (InvalidArgumentException | MetadataObjectNotFoundException
                        | BusinessObjectNotFoundException | ApplicationObjectNotFoundException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error")
                            , ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                }
                return childrenNode;
            }
        };
        // we load the data
        navTree.createDataProvider(aNode);
        
        navTree.addThemeVariants(GridVariant.LUMO_COMPACT);
        //navTree.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT); uncomment to change the grid variants
        navTree.setSelectionMode(Grid.SelectionMode.SINGLE);
        navTree.setId("navTreeCenter");
        navTree.setHeightByRows(true);
        navTree.setPageSize(10);
        
        navTree.addComponentHierarchyColumn(item
                -> new IconLabelCellGrid(item, item.isPool(), item.isSelected(), iconGenerator));
        
        navTree.addComponentColumn(item -> createActions(item))
                .setTextAlign(ColumnTextAlign.END).setFlexGrow(0);

        navTree.setSelectionMode(Grid.SelectionMode.SINGLE);
        navTree.addItemClickListener(event -> {
            nodeSelected = event.getItem();
            if (!navTree.getSelectedItems().isEmpty())
                buildDetailsPanel(navTree.getSelectedItems().stream().findFirst().orElse(null));
            if (nodeSelected.isPool()) {
                event.getItem().setSelected(true);
                navTree.getDataProvider().refreshItem(nodeSelected);
            }
        });

        navTree.addExpandListener(event -> {
            event.getItems().stream().findFirst().ifPresent(item -> {
                nodeSelected = item;
                buildDetailsPanel(item);
                if(item.isCustomer())
                    navTree.getDataProvider().refreshItem(item);
                if(!item.isPool() && item.isService())
                    currentService = (BusinessObjectLight) item.getObject();
                if (nodeSelected.isPool()) {
                    item.setSelected(true);
                    navTree.getDataProvider().refreshItem(nodeSelected); 
                }
            });
        });
        
        navTree.addCollapseListener(event -> {
            event.getItems().stream().findFirst().ifPresent(item -> {
                buildDetailsPanel(item);
                if(item.isCustomer()) {
                    navTree.getDataProvider().refreshItem(item);
                }
                if (nodeSelected.isPool()) {
                    item.setSelected(false);
                    navTree.getDataProvider().refreshItem(nodeSelected);
                }
            });
        });

        HorizontalLayout lytTitle = new HorizontalLayout();
        lytTitle.setPadding(false);
        lytTitle.setMargin(false);
        lytTitle.setSpacing(true);
        lytTitle.setHeight("25px");
        lytTitle.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        lytTitle.setClassName("serviceman-center-header-lyt");
      
        if (nodeSelected.isPool()) {
            Icon icon = new Icon(VaadinIcon.FOLDER_OPEN);
            icon.setSize("18px");
            lytTitle.add(icon, new Label(nodeSelected.getName()));
        } else {
            IconLabelCellGrid icon = new IconLabelCellGrid(nodeSelected, nodeSelected.isPool(), nodeSelected.isSelected(), iconGenerator);
            lytTitle.add(icon);
        }

        lytCenter.add(lytTitle);
        lytCenter.add(navTree);
        lytCenter.setVisible(true);
    }

    /**
     * Create actions for the grids
     * @param node the selected item in the grid
     * @return a horizontal layout with the button actions
     */
    private Component createActions(ServiceManTreeNode node) {
        HorizontalLayout lytActions = new HorizontalLayout();
        lytActions.setHeight("22px");
        lytActions.setJustifyContentMode(JustifyContentMode.END);
        lytActions.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        lytActions.setPadding(false);
        lytActions.setMargin(false);
        lytActions.setSpacing(false);

        if (node.isPool() && node.isCustomer()) {
            ActionButton btnDelete = new ActionButton(new ActionIcon(VaadinIcon.TRASH),
                    this.deleteCustomerPoolVisualAction.getModuleAction().getDisplayName());
            Command deleteCustomerPool = () -> refreshCustomerPool(true);
            btnDelete.addClickListener(event -> {
                actionAffectedNode.set(0, node);
                this.deleteCustomerPoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(PARAMETER_CUSTOMER_POOL, node.getObject()),
                        new ModuleActionParameter("deleteCustomerPool", deleteCustomerPool)
                )).open();
            });
            btnDelete.setHeight("20px");
            btnDelete.setWidth("20px");

            ActionButton btnAdd = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O),
                    this.newCustomerVisualAction.getModuleAction().getDisplayName());
            btnAdd.addClickListener(event -> {
                actionAffectedNode.set(0, node);
                this.newCustomerVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(PARAMETER_CUSTOMER_POOL, node.getObject()))).open();
            });
            btnAdd.setHeight("20px");
            btnAdd.setWidth("20px");

            lytActions.add(btnAdd, btnDelete);
        } else if (node.isPool() && node.isService()) {
            ActionButton btnDelete = new ActionButton(new ActionIcon(VaadinIcon.TRASH),
                    this.deleteServicePoolVisualAction.getModuleAction().getDisplayName());
            btnDelete.addClickListener(event -> {
                actionAffectedNode.set(0, node);
                nodeSelected = node;
                this.deleteServicePoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(PARAMETER_SERVICE_POOL, node.getObject())
                )).open();
            });
            btnDelete.setHeight("20px");
            btnDelete.setWidth("20px");

            ActionButton btnAdd = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O),
                    this.newServiceVisualAction.getModuleAction().getDisplayName());
            btnAdd.addClickListener(event -> {
                actionAffectedNode.set(0, node);
                List<BusinessObjectLight> parents = null;
                try {
                    InventoryObjectPool servicePool = (InventoryObjectPool) node.getObject();
                    parents = bem.getParents(servicePool.getClassName(), servicePool.getId());
                } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                }
                if (parents != null && !parents.isEmpty()) {
                    final BusinessObjectLight poolParent = parents.get(0);
                    this.newServiceVisualAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter(PARAMETER_CUSTOMER, poolParent),
                            new ModuleActionParameter(PARAMETER_SERVICE_POOL, node.getObject())
                    )).open();
                } else {
                    this.newServiceVisualAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter(PARAMETER_SERVICE_POOL, node.getObject())
                    )).open();
                }
            });
            btnAdd.setHeight("20px");
            btnAdd.setWidth("20px");
            
            lytActions.add(btnAdd, btnDelete);
        } else if (!node.isPool() && node.isCustomer()) {
            ActionButton btnDelete = new ActionButton(new ActionIcon(VaadinIcon.TRASH),
                    this.deleteCustomerVisualAction.getModuleAction().getDisplayName());
            btnDelete.addClickListener(event -> {
                actionAffectedNode.set(0, node);
                nodeSelected = node;
                this.deleteCustomerVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(PARAMETER_CUSTOMER, node.getObject())
                )).open();
            });
            btnDelete.setHeight("20px");
            btnDelete.setWidth("20px");

            ActionButton btnAdd = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O),
                    this.newServicePoolVisualAction.getModuleAction().getDisplayName());
            btnAdd.addClickListener(event -> {
                actionAffectedNode.set(0, node);
                this.newServicePoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(PARAMETER_SERVICE, node.getObject())
                )).open();
            });
            btnAdd.setHeight("20px");
            btnAdd.setWidth("20px");
            
            lytActions.add(btnAdd, btnDelete);
        } else if (!node.isPool() && node.isService()) {
            ActionButton btnDelete = new ActionButton(new ActionIcon(VaadinIcon.TRASH),
                    this.deleteServiceVisualAction.getModuleAction().getDisplayName());
            btnDelete.addClickListener(event -> {
                actionAffectedNode.set(0, node);
                nodeSelected = node;
                this.deleteServiceVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(PARAMETER_SERVICE, node.getObject())
                )).open();
            });
            btnDelete.setHeight("20px");
            btnDelete.setWidth("20px");
            
            lytActions.add(btnDelete);
        } else {
            ActionButton btnReleaseItem = new ActionButton(new ActionIcon(VaadinIcon.UNLINK),
                    this.releaseObjectFromServiceVisualAction.getModuleAction().getDisplayName());
            btnReleaseItem.addClickListener(event -> {
                actionAffectedNode.set(0, node);
                nodeSelected = node;
                this.releaseObjectFromServiceVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter<>(PARAMETER_SERVICE, currentService),
                        new ModuleActionParameter<>(PARAMETER_BUSINESS_OBJECT, node.getObject())
                )).open();
            });
            btnReleaseItem.setHeight("20px");
            btnReleaseItem.setWidth("20px");
            
            lytActions.add(btnReleaseItem);
        }
        return lytActions;
    }

    /**
     * Creates the menu action for root elements
     */
    public void createLeftGridAction() {
        Command addCustomerPool = () -> refreshCustomerPool(false);
        btnAddCustomerPool.addClickListener(event -> {
            this.newCustomerPoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter<>("addCustomerPool", addCustomerPool)
            )).open();
        });
    }
    
    /**
     * Creates the right-most layout with the options for the selected object
     * @param selectedObject the selected object in the nav tree
     */
    private void buildDetailsPanel(ServiceManTreeNode selectedObject) {
        try {
            lytDetailsPanel.removeAll();
            // Header
            Label lblTitle = new Label(selectedObject.getName());
            lblTitle.setClassName("serviceman-property-sheet-details");
            
            if (!selectedObject.isPool()) {
                ObjectOptionsPanel pnlOptions = new ObjectOptionsPanel((BusinessObjectLight) selectedObject.getObject(),
                        actionRegistry, advancedActionsRegistry, viewWidgetRegistry, explorerWidgetRegistry, mem, aem, bem, ts);
                pnlOptions.setShowViews(false);
                pnlOptions.setShowExplorers(true);
                pnlOptions.setSelectionListener((event) -> {
                    switch (event.getActionCommand()) {
                        case ObjectOptionsPanel.EVENT_ACTION_SELECTION:
                            ModuleActionParameterSet parameters = new ModuleActionParameterSet(new ModuleActionParameter<>("businessObject", selectedObject.getObject()));
                            Dialog wdwObjectAction = (Dialog) ((AbstractVisualInventoryAction) event.getSource())
                                    .getVisualComponent(parameters);
                            wdwObjectAction.open();
                            break;
                        case ObjectOptionsPanel.EVENT_EXPLORER_SELECTION:
                            Dialog wdwExplorer = new Dialog(
                                    ((AbstractExplorerWidget) event.getSource())
                                            .build((BusinessObjectLight) selectedObject.getObject())
                            );
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
                    Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                    Object lastValue =  pnlOptions.lastValue(property.getName());
                    HashMap<String, String> attributes = new HashMap<>();
                    attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));
                    if (selectedObject.isCustomer()) {
                        try {
                            sms.updateCustomer(selectedObject.getClassName(), selectedObject.getId(), attributes, session.getUser().getUserName());
                            if (property.getName().equals(Constants.PROPERTY_NAME)) {
                                nodeSelected.setName(String.valueOf(property.getValue()));
                                ((BusinessObjectLight) nodeSelected.getObject()).setName(String.valueOf(property.getValue()));
                            }
                            // activity log
                            aem.createObjectActivityLogEntry(session.getUser().getUserName(), selectedObject.getClassName(),
                                    selectedObject.getId(), ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT,
                                    property.getName(), lastValue == null ? "" : lastValue.toString(), property.getAsString(), "");
                        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | OperationNotPermittedException
                                | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                                    AbstractNotification.NotificationType.ERROR, ts).open();
                            pnlOptions.UndoLastEdit();
                        }
                    } else if (selectedObject.isService()) {
                        try {
                            sms.updateService(selectedObject.getClassName(), selectedObject.getId(), attributes, session.getUser().getUserName());
                            if (property.getName().equals(Constants.PROPERTY_NAME)) {
                                nodeSelected.setName(String.valueOf(property.getValue()));
                                ((BusinessObjectLight) nodeSelected.getObject()).setName(String.valueOf(property.getValue()));
                            }
                            // activity log
                            aem.createObjectActivityLogEntry(session.getUser().getUserName(), selectedObject.getClassName(),
                                    selectedObject.getId(), ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT,
                                    property.getName(), lastValue == null ? "" : lastValue.toString(), property.getAsString(), "");
                        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | OperationNotPermittedException
                                | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                                    AbstractNotification.NotificationType.ERROR, ts).open();
                            pnlOptions.UndoLastEdit();
                        }
                    } 
                    if (navTree.getTreeData().contains(nodeSelected))
                        navTree.getDataProvider().refreshItem(nodeSelected);
                    buildDetailsPanel(nodeSelected);
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                            ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                            AbstractNotification.NotificationType.INFO, ts).open();
                });

                HorizontalLayout lytExtraActions = new HorizontalLayout();
                lytExtraActions.setWidthFull();

                Button btnGoToDashboard = new Button(ts.getTranslatedString("module.navigation.widgets.object-dashboard.open-to-dashboard"));
                Button btnInfo = new Button(ts.getTranslatedString("module.navigation.actions.show-more-information-button-name"));
                
                // Action go to Dashboard                
                btnGoToDashboard.addClickListener(ev -> {
                    getUI().ifPresent(ui -> {
                        ui.getSession().setAttribute(BusinessObjectLight.class, (BusinessObjectLight) selectedObject.getObject());
                        ui.getPage().open(RouteConfiguration.forRegistry(VaadinService.getCurrent().getRouter().getRegistry()).getUrl(ObjectDashboard.class), "_blank");
                    });
                });

                btnInfo.addClickListener(e -> {
                    this.windowMoreInformation.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("object", selectedObject.getObject()))).open();
                });
                
                lytExtraActions.addAndExpand(btnGoToDashboard, btnInfo);    

                // Add content to layout
                lytDetailsPanel.add(lblTitle, lytExtraActions, pnlOptions.build(UI.getCurrent().getSession().getAttribute(Session.class).getUser()));
            } else if (selectedObject.isPool()) {
                propertySheetPool = new PropertySheet(ts, new ArrayList<>());
                propertySheetPool.addPropertyValueChangedListener(this);
                if (selectedObject.isCustomer()) {
                    InventoryObjectPool aWholeCustomerPool = sms.getCustomerPool(selectedObject.getId(), selectedObject.getClassName());
                    propertySheetPool.setItems(PropertyFactory.propertiesFromPoolWithoutClassName((InventoryObjectPool) aWholeCustomerPool, ts));
                } else if (selectedObject.isService()) {
                    InventoryObjectPool aWholeServicePool = sms.getServicePool(selectedObject.getId(), selectedObject.getClassName());
                    propertySheetPool.setItems(PropertyFactory.propertiesFromPoolWithoutClassName((InventoryObjectPool) aWholeServicePool, ts));
                }
                lytDetailsPanel.add(lblTitle, propertySheetPool); 
            }         
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    private void loadItemTreeTags(ServiceManTreeNode item) {
        boolean service;
        boolean customer;
        try {
            service = mem.isSubclassOf(Constants.CLASS_GENERICSERVICE, item.getClassName());
            customer = mem.isSubclassOf(Constants.CLASS_GENERICCUSTOMER, item.getClassName());
            if(service) {
                Icon icon = new Icon(VaadinIcon.FOLDER);
                item.setIcon(icon);                
            } else {
                Image objIcon = new Image(StreamResourceRegistry.getURI(iconGenerator.apply( item.getClassName())).toString(), "-");
                item.setImage(objIcon);
            }
            if(item.getObject() instanceof InventoryObjectPool)
                item.setDescription(((InventoryObjectPool)item.getObject()).getDescription());
            item.setService(service);
            item.setCustomer(customer);
            
            item.setPool(item.getObject() instanceof InventoryObjectPool);
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    /**
     * remove old elements inside layouts
     */
    private void clearElements() {
        lytParentsLocation.removeAll();
        lytParentsDevice.removeAll();
        lytLeftSide.removeAll();
        lytCenter.removeAll();
        lytDetailsPanel.removeAll();
    }
   
    @Override
    public void updatePropertyChanged(AbstractProperty<? extends Object> property) {
        Session session = UI.getCurrent().getSession().getAttribute(Session.class);
        if (nodeSelected != null) {
            if (nodeSelected.isPool()) {
                if (nodeSelected.isCustomer()) {
                    try {
                        if (property.getName().equals(Constants.PROPERTY_NAME)) {
                            sms.updateCustomerPool(nodeSelected.getId(), nodeSelected.getClassName(),
                                    String.valueOf(property.getValue()), nodeSelected.getDescription(), session.getUser().getUserName());
                            nodeSelected.setName(String.valueOf(property.getValue()));
                            ((InventoryObjectPool)nodeSelected.getObject()).setName(String.valueOf(property.getValue()));
                        } else if (property.getDescription().equals(Constants.PROPERTY_DESCRIPTION)) {
                            sms.updateCustomerPool(nodeSelected.getId(), nodeSelected.getClassName(), nodeSelected.getName(),
                                    String.valueOf(property.getValue()), session.getUser().getUserName());
                            nodeSelected.setDescription(String.valueOf(property.getValue()));
                        }
                        gridLeftNav.getDataProvider().refreshItem(nodeSelected);
                    } catch (ApplicationObjectNotFoundException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                                AbstractNotification.NotificationType.ERROR, ts).open();
                        propertySheetPool.undoLastEdit();
                    }
                } else if (nodeSelected.isService()) {
                    try {
                        if (property.getName().equals(Constants.PROPERTY_NAME)) {
                        sms.updateServicePool(nodeSelected.getId(), nodeSelected.getClassName(),
                                String.valueOf(property.getValue()), nodeSelected.getDescription(), session.getUser().getUserName());
                        nodeSelected.setName(String.valueOf(property.getValue()));
                        ((InventoryObjectPool)nodeSelected.getObject()).setName(String.valueOf(property.getValue()));
                        } else if (property.getDescription().equals(Constants.PROPERTY_DESCRIPTION)) {
                            sms.updateServicePool(nodeSelected.getId(), nodeSelected.getClassName(), nodeSelected.getName(),
                                    String.valueOf(property.getValue()), session.getUser().getUserName());
                            nodeSelected.setDescription(String.valueOf(property.getValue()));
                        }
                        if (navTree.getTreeData().contains(nodeSelected))
                            navTree.getDataProvider().refreshItem(nodeSelected);
                    } catch (ApplicationObjectNotFoundException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                                AbstractNotification.NotificationType.ERROR, ts).open();
                        propertySheetPool.undoLastEdit();
                    }
                }
                buildDetailsPanel(nodeSelected);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                        ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                        AbstractNotification.NotificationType.INFO, ts).open();
            }
        }
    }
    
    /**
     * Refresh customer data provider if an element is added.
     * If an element is removed, cleans the different layouts and they are built again. 
     * @param deleted Validate if an element is removed
     */
    private void refreshCustomerPool(boolean deleted) {
        try {
            List<InventoryObjectPool> customerPools = sms.getCustomerPools();
            List<ServiceManTreeNode> items = customerPools.stream().map(item -> {
                ServiceManTreeNode firstNode = new ServiceManTreeNode(item);
                firstNode.setPool(true);
                loadItemTreeTags(firstNode);
                return firstNode;
            }).collect(Collectors.toList());

            if (deleted) {
                clearElements();
                buildLeftNavGrid();
                createLeftGridAction();
                gridLeftNav.setItems(items);
                gridLeftNav.getDataProvider().refreshAll();
                lytLeftSide.addComponentAsFirst(createParentBreadCrumbs(items, 1));
                searchDialog.clearSearch();
            } else {
                gridLeftNav.setItems(items);
                gridLeftNav.getDataProvider().refreshAll();
                lytLeftSide.addComponentAsFirst(createParentBreadCrumbs(items, 1));
                searchDialog.clearSearch();
            }
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
}