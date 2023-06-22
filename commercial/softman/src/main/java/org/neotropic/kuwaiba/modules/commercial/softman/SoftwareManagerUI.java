/*
 * Copyright 2010-2023. Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neotropic.kuwaiba.modules.commercial.softman;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinService;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
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
import org.neotropic.kuwaiba.modules.commercial.softman.actions.RelateToLicenseVisualAction;
import org.neotropic.kuwaiba.modules.commercial.softman.actions.ReleaseRelationshipVisualAction;
import org.neotropic.kuwaiba.modules.commercial.softman.components.EActionParameter;
import org.neotropic.kuwaiba.modules.commercial.softman.components.LicenseManagerDialog;
import org.neotropic.kuwaiba.modules.commercial.softman.explorers.DialogSoftwareManagerSearch;
import org.neotropic.kuwaiba.modules.commercial.softman.nodes.SoftwareObjectNode;
import org.neotropic.kuwaiba.modules.commercial.softman.visual.IconLabelCellGrid;
import org.neotropic.kuwaiba.modules.core.navigation.ObjectDashboard;
import org.neotropic.kuwaiba.modules.core.navigation.actions.ShowMoreInformationAction;
import org.neotropic.kuwaiba.modules.optional.reports.actions.LaunchClassLevelReportAction;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main for the Software Manager module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route(value = "softman", layout = SoftwareManagerLayout.class) 
public class SoftwareManagerUI extends VerticalLayout implements ActionCompletedListener,
        PropertySheet.IPropertyValueChangedListener, HasDynamicTitle, AbstractUI {
    /**
     * Reference to the Translation Service
     */
    @Autowired
    private TranslationService ts;    
    /**
     * Reference to the Application Entity Manager
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Metadata Entity Manager
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Business Entity Manager
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * The window to show more information about an object.
     */
    @Autowired
    private ShowMoreInformationAction windowMoreInformation;  
    /**
     * The visual action for license management.
     */
    @Autowired
    private LicenseManagerDialog licenseManagerDialog;
    /**
     * Launches class level reports given a selected object.
     */
    @Autowired
    private LaunchClassLevelReportAction launchClassLevelReportAction;
    /**
     * The visual action to relate an object to license.
     */
    @Autowired
    private RelateToLicenseVisualAction relateToLicenseVisualAction;
    /**
     * The visual action to release a relationship.
     */
    @Autowired
    private ReleaseRelationshipVisualAction releaseRelationshipVisualAction;
    /**
     * Factory to build resources from data source.
     */
    @Autowired
    private ResourceFactory resourceFactory;
    /**
     * Split the content
     */
    private SplitLayout splitLayout;
    /**
     * Left side layout
     */
    private VerticalLayout lytLeftMain;
    private VerticalLayout lytLeftSide;
    private VerticalLayout lytLeftResult;
    /**
     * Center layout
     */
    private VerticalLayout lytCenter;
    /**
     * Right side layout
     */
    private VerticalLayout lytDetails;
    private VerticalLayout lytPropertySheet;
    private VerticalLayout lytRelationship;
    /**
     * Left grid with search results
     */
    private Grid<SoftwareObjectNode> gridLeftNav;
    /**
     * The grid with the list objects
     */
    private Grid<BusinessObjectLight> gridObjects;
    /**
     * The grid with the list licenses
     */
    private Grid<BusinessObjectLight> gridLicenses;
    /**
     * The list objects
     */
    private List<BusinessObjectLight> listObjects;
    /**
     * Property sheet
     */
    private PropertySheet propertysheet;
    /**
     * Used to save the selected license
     */
    private BusinessObjectLight currentLicense;
    /**
     * Used to save the selected object
     */
    private BusinessObjectLight currentObject;
    /**
     * A button to edit, add, remove and release the existing software licenses.
     */
    private ActionButton btnManageLicenses;
    /**
     * An icon generator for create icons
     */
    private ClassNameIconGenerator iconGenerator;
    /**
     * display Number of classes for page
     */
    private static final int RESULTS_CLASSES_PER_PAGE = 5;
    /**
     * The component to show the results of a search
     */
    private DialogSoftwareManagerSearch searchDialog;
    /**
     * Used to display information about objects
     */
    private Label lblObjects;
    /***
     * Used to display the selected license name
     */
    private Label lblLicense;
    
    public SoftwareManagerUI() {
        super();
        setSizeFull();
    }
    
    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS)
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                    AbstractNotification.NotificationType.INFO, ts).open();
        else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();  
    }
    
    @Override
    public void onDetach(DetachEvent ev) {
        this.relateToLicenseVisualAction.unregisterListener(this);
        this.releaseRelationshipVisualAction.unregisterListener(this);
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.softman.title");
    }

    @Override
    public void initContent() {
        // Register action
        this.relateToLicenseVisualAction.registerActionCompletedLister(this);
        this.releaseRelationshipVisualAction.registerActionCompletedLister(this);
        
        // --> Init Left Layout
        createManageLicensesButton();        
        VerticalLayout lytOptions = new VerticalLayout();
        lytOptions.setClassName("left-action-combobox");
        lytOptions.setSpacing(true);
        lytOptions.setMargin(false);
        lytOptions.setPadding(false);
        lytOptions.add(btnManageLicenses);
        // Left side layout
        lytLeftSide = new VerticalLayout();
        lytLeftSide.setId("left-side");
        lytLeftSide.setMargin(false);
        lytLeftSide.setSpacing(false);
        // Result layout
        lytLeftResult = new VerticalLayout();
        lytLeftResult.setId("left-result");
        lytLeftResult.setMargin(false);
        lytLeftResult.setSpacing(false);
        // Main left layout
        lytLeftMain = new VerticalLayout(lytOptions);
        lytLeftMain.setSizeFull();
        lytLeftMain.setMargin(false);
        lytLeftMain.setSpacing(false);
        lytLeftMain.setId("lyt-left-main");
        setupSearchBar();
        // End Left Layout <--
        
        // --> Init Center Layout
        lytCenter = new VerticalLayout();
        lytCenter.setId("lyt-center");
        lytCenter.setWidth("50%");
        lytCenter.setVisible(false);
        
        lblObjects = new Label();
        // End Center Layout <--
        
        // --> Init Right Layout
        // Property Sheet
        propertysheet = new PropertySheet(ts, new ArrayList<>());
        propertysheet.addPropertyValueChangedListener(this);
        lytPropertySheet = new VerticalLayout();
        lytPropertySheet.setId("lyt-property-sheet");
        lytPropertySheet.setWidthFull();
        lytPropertySheet.setBoxSizing(BoxSizing.BORDER_BOX);
        lytPropertySheet.setMargin(false);
        lytPropertySheet.setPadding(false);
        lytPropertySheet.setVisible(false);
        // Details
        lytDetails = new VerticalLayout();
        lytDetails.setId("lyt-details");
        lytDetails.setWidth("50%");
        lytDetails.setSpacing(true);        
        // Relationship
        lytRelationship = new VerticalLayout();
        lytRelationship.setId("lyt-relationship");
        lytRelationship.setPadding(false);
        lytRelationship.setMargin(false);
        lytRelationship.setSpacing(true);
        lytRelationship.setHeightFull();        
        //
        HorizontalLayout lytRightMain = new HorizontalLayout();
        lytRightMain.setClassName("rigth-side");
        lytRightMain.setId("right-lyt");
        lytRightMain.setPadding(false);
        lytRightMain.setMargin(false);
        lytRightMain.setSpacing(true);
        lytRightMain.add(lytCenter, lytDetails);
        // End Right Layout <--
        
        // Split layout
        splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(25);
        // Add content
        splitLayout.addToPrimary(lytLeftMain);
        splitLayout.addToSecondary(lytRightMain);
        add(splitLayout);
    }
    
    private void createManageLicensesButton() {
        btnManageLicenses = new ActionButton(ts.getTranslatedString("module.softman.actions.manage-licenses.name"));
        btnManageLicenses.setToolTip(ts.getTranslatedString("module.softman.actions.manage-licenses.description"));
        btnManageLicenses.setWidthFull();
        btnManageLicenses.addClickListener(event -> launchLicenseDialog());
        btnManageLicenses.setHeight("32px");
    }
    
    private void launchLicenseDialog() {
        this.licenseManagerDialog.getVisualComponent(new ModuleActionParameterSet()).open();
    }
    
    /**
     * Setups the search bar
     */
    private void setupSearchBar() {
        iconGenerator = new ClassNameIconGenerator(resourceFactory);
        searchDialog = new DialogSoftwareManagerSearch(ts, aem, bem, iconGenerator, e -> {
            lytLeftSide.removeAll();
            if (e instanceof String)// No suggestion was chosen
                processSearch((String) e, 0, RESULTS_CLASSES_PER_PAGE);
            else {// A single element was selected
                clearElements();
                List<SoftwareObjectNode> list = new ArrayList<>();
                if (e != null && e instanceof BusinessObjectLight) {
                    SoftwareObjectNode firstNode = new SoftwareObjectNode((BusinessObjectLight) e);
                    list.add(firstNode);
                }
                
                buildLeftNavGrid();
                gridLeftNav.setItems(list);
                lytLeftSide.addComponentAsFirst(createParentBreadCrumbs(list, 1));
            }
            searchDialog.close();
        });
        searchDialog.setWidthFull();
        
        HorizontalLayout lytSearchBar = new HorizontalLayout(searchDialog);
        lytSearchBar.setWidthFull();
        lytSearchBar.setPadding(false);
        lytSearchBar.setMargin(false);
        
        lytLeftMain.add(lytSearchBar);
        lytLeftMain.add(lytLeftSide);
    }

    /**
     * After a search the searched text is process to create a result of
     * business objects grouped by class name in grids
     * @param searchedText the searched text
     * @param skip the skip per pagination
     * @param limit the limit per pagination
     */
    private void processSearch(String searchedText, int skip, int limit) {
        try {
            List<BusinessObjectLight> searchObjectResult = null;
            if (!searchedText.isEmpty())
                searchObjectResult = bem.getSuggestedObjectsWithFilter(
                        searchedText, skip, limit,
                        Constants.CLASS_GENERICSOFTWAREASSET,
                        Constants.CLASS_GENERICCOMMUNICATIONSELEMENT,
                        EActionParameter.SOFTWARE_TYPE.getPropertyValue()
                );
            lytLeftSide.removeAll();
            if (searchObjectResult == null || searchObjectResult.isEmpty())
                lytLeftSide.add(new Label(ts.getTranslatedString("module.general.messages.no-search-results")));
            else {
                buildLeftNavGrid();
                List<SoftwareObjectNode> items = searchObjectResult.stream().map(item -> {
                    SoftwareObjectNode firstNode = new SoftwareObjectNode(item);
                    return firstNode;
                }).collect(Collectors.toList());
                gridLeftNav.setItems(items);
                lytLeftSide.addComponentAsFirst(createParentBreadCrumbs(items, 1));
            }
        } catch (Exception ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    /**
     * Creates the grid with the search result
     */
    private void buildLeftNavGrid() {
        lytLeftResult.removeAll();
        
        gridLeftNav = new Grid<>();
        gridLeftNav.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        gridLeftNav.setSelectionMode(Grid.SelectionMode.SINGLE);

        gridLeftNav.addComponentColumn(obj
                -> new IconLabelCellGrid( obj, obj.isPool(), iconGenerator));
        gridLeftNav.addItemClickListener(event ->  buildObjectsGrid(event.getItem()));
        
        lytLeftResult.add(gridLeftNav);
        lytLeftMain.add(lytLeftResult);
    }
    
    private void buildObjectsGrid(SoftwareObjectNode node) {
        lytCenter.removeAll();
        lytDetails.removeAll();
        
        gridObjects = new Grid<>();
        gridObjects.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        gridObjects.setSelectionMode(Grid.SelectionMode.SINGLE);
        gridObjects.setPageSize(10);
        
        Grid.Column<BusinessObjectLight> name = gridObjects.addColumn(TemplateRenderer.<BusinessObjectLight>of(
                            "<div>[[item.name]] &nbsp; <font class=\"text-secondary\">[[item.className]]</font></div>")
                            .withProperty(EActionParameter.NAME.getPropertyValue(), BusinessObjectLight::getName)
                            .withProperty(EActionParameter.CLASS_NAME.getPropertyValue(), BusinessObjectLight::getClassName));
        
        createDataProvider(node, name);
        gridObjects.addItemClickListener(event -> loadDetails(event.getItem()));
        
        lytCenter.add(gridObjects);
        lytCenter.add(lblObjects);
        lytCenter.setVisible(true);
    }
    
    private void refreshObjectsGrid(BusinessObjectLight object) {
        loadObjects(object);
        gridObjects.setItems(listObjects);
        gridObjects.getDataProvider().refreshAll();
    }
    
    private void createDataProvider(SoftwareObjectNode node, Grid.Column<BusinessObjectLight> column) {
        try {
            boolean software = mem.isSubclassOf(Constants.CLASS_GENERICSOFTWAREASSET, node.getClassName());
            boolean object = mem.isSubclassOf(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, node.getClassName());
            boolean type = mem.isSubclassOf(EActionParameter.SOFTWARE_TYPE.getPropertyValue(), node.getClassName());
            
            if (node.getObject() instanceof BusinessObjectLight) {
                BusinessObjectLight businessObject = (BusinessObjectLight) node.getObject();
                if (software) {
                    currentLicense = businessObject;
                    loadObjects(businessObject);
                    
                    if (column != null)
                        column.setHeader(ts.getTranslatedString("module.softman.label-inventory-objects"));

                    gridObjects.addComponentColumn(item -> createObjectAction(item, businessObject, true,
                                    false, true))
                            .setTextAlign(ColumnTextAlign.END).setFlexGrow(0);
                    
                    if (listObjects.isEmpty()) {
                        lblObjects.setText(ts.getTranslatedString("module.general.label.no-related-inventory-objects"));
                        lblObjects.setVisible(true);
                    } else 
                        lblObjects.setVisible(false);
                } else if (object) {
                    currentObject = businessObject;
                    loadObjects(businessObject);
                    
                    if (column != null)
                        column.setHeader(ts.getTranslatedString("module.softman.label-licenses"));
                    
                    gridObjects.addComponentColumn(item -> createObjectAction(item, businessObject, true,
                                    true, true)).setTextAlign(ColumnTextAlign.END).setFlexGrow(0)
                            .setWidth("150px");
                    
                    if (listObjects.isEmpty()) {
                        lblObjects.setText(ts.getTranslatedString("module.general.label.no-related-licenses"));
                        lblObjects.setVisible(true);
                    } else 
                        lblObjects.setVisible(false);
                        
                } else if (type) {
                    lblObjects.setText(ts.getTranslatedString("module.softman.label-inventory-objects"));
                    loadCommunicationsElement(businessObject);
                    
                    if (column != null)
                        column.setHeader(ts.getTranslatedString("module.softman.label-inventory-objects"));
                    
                    gridObjects.addComponentColumn(item -> createObjectAction(item, null, false,
                                    false, true)).setTextAlign(ColumnTextAlign.END).setFlexGrow(0);
                    
                    if (listObjects.isEmpty()) {
                        lblObjects.setText(ts.getTranslatedString("module.general.label.no-related-inventory-objects"));
                        lblObjects.setVisible(true);
                    } else 
                        lblObjects.setVisible(false);
                }
                
                gridObjects.setItems(listObjects);
            }
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                             ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void loadObjects(BusinessObjectLight businessObject) {
        try {
            listObjects = bem.getSpecialAttribute(businessObject.getClassName(), businessObject.getId(),
                    EActionParameter.LICENSE_HAS.getPropertyValue());
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void loadCommunicationsElement(BusinessObjectLight businessObject) {
        try {
            List<BusinessObjectLight> items = aem.getListTypeItemUses(businessObject.getClassName(), businessObject.getId(), -1);
            items.forEach(this::loadObjects);
        } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    /**
     * Creates/updates the localization path, that shows the whole list 
     * of the parents of the selected object
     * @param selectedItemParents the selected object in the location
     * @param kind the kind of bread crumbs if is location or device
     */
    private Div createParentBreadCrumbs(List<SoftwareObjectNode> selectedItemParents, int  kind) {
        Div divPowerline = new Div();
        divPowerline.setWidthFull();
        divPowerline.setClassName("serviceman-parents-breadcrumbs");

        Collections.reverse(selectedItemParents);
        selectedItemParents.forEach(parent -> {
            if (parent.getObject() instanceof BusinessObjectLight) {
                Span span = new Span(new Label(parent.getClassName().equals(Constants.DUMMY_ROOT) ? "/" : parent.getName()));
                span.setSizeUndefined();
                span.setTitle(String.format("[%s]", parent.getClassName()));
                span.addClassNames("parent", kind == 1 ? "location-parent-color" : "device-parent-color");
                divPowerline.add(span);
            }
        });

        return divPowerline;
    }
    
    private void loadDetails(BusinessObjectLight businessObject) {
        try {
            lytDetails.removeAll();
            if (businessObject != null) {
                boolean object = mem.isSubclassOf(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, businessObject.getClassName());
                boolean software = mem.isSubclassOf(Constants.CLASS_GENERICSOFTWAREASSET, businessObject.getClassName());
                
                if (object) { // Build the grid of licenses associated to the selected object
                    lytRelationship.removeAll();

                    gridLicenses = new Grid<>();
                    gridLicenses.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
                    gridLicenses.setSelectionMode(Grid.SelectionMode.SINGLE);
                    gridLicenses.setHeightByRows(true);
                    gridLicenses.setPageSize(10);

                    loadObjects(businessObject);
                    ListDataProvider<BusinessObjectLight> dataProvider = new ListDataProvider<>(listObjects);
                    gridLicenses.setDataProvider(dataProvider);

                    gridLicenses.addColumn(TemplateRenderer.<BusinessObjectLight>of(
                            "<div>[[item.name]] &nbsp; <font class=\"text-secondary\">[[item.className]]</font></div>")
                            .withProperty(EActionParameter.NAME.getPropertyValue(), BusinessObjectLight::getName)
                            .withProperty(EActionParameter.CLASS_NAME.getPropertyValue(), BusinessObjectLight::getClassName))
                            .setHeader(ts.getTranslatedString("module.softman.label-licenses"));
                    
                    gridLicenses.addComponentColumn(item -> createObjectAction(item, businessObject,true,
                                    true, false)).setTextAlign(ColumnTextAlign.END).setFlexGrow(0).setWidth("150px");

                    currentObject = businessObject;
                    lytRelationship.add(gridLicenses);
                    
                    if(listObjects.isEmpty()) {
                        Label lblInfo = new Label(ts.getTranslatedString("module.general.label.no-related-licenses"));
                        lytRelationship.add(lblInfo);
                    }
                        
                    lytDetails.add(lytRelationship);
                } else if (software) { // Shows the properties of the selected license
                    currentLicense = businessObject;
                    updatePropertySheet(businessObject);
                    
                    lytPropertySheet.setVisible(true);
                    lytDetails.add(lytPropertySheet);
                }
            }
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
        
    private void updatePropertySheet(BusinessObjectLight businessObject) {
        if (businessObject != null) {
            try {
                lytPropertySheet.removeAll();
                BusinessObject aWholeLicense = bem.getObject(businessObject.getClassName(), businessObject.getId());
                propertysheet.setItems(PropertyFactory.propertiesFromBusinessObject(aWholeLicense, ts, aem, mem));
                
                lblLicense = new Label(aWholeLicense.getName());
                lblLicense.setClassName("softman-license-property-sheet-header");
                
                lytPropertySheet.add(lblLicense, propertysheet);
            } catch (InventoryException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
    }
    
    @Override
    public void updatePropertyChanged(AbstractProperty<?> property) {
        try {
            if (currentObject != null && currentLicense != null) {
                HashMap<String, String> attributes = new HashMap<>();
                attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));
                bem.updateObject(currentLicense.getClassName(), currentLicense.getId(), attributes);
                if (property.getName().equals(Constants.PROPERTY_NAME)) {
                    currentLicense.setName(String.valueOf(property.getValue()));
                    lblLicense.setText(String.valueOf(property.getValue()));
                    refreshObjectsGrid(currentObject);
                }
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                        ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                        AbstractNotification.NotificationType.INFO, ts).open();
            }
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | OperationNotPermittedException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
            propertysheet.undoLastEdit();
        }
    }
    
    /**
     * Creates a component and add actions according to specifications or source object type.
     * @param sourceObject Object to which the action is applied.
     * @param targetObject The object we want to be released from. If any relationship exists.
     * @param release True if release action is added.
     * @param reports True if reports action is added.
     * @param isObjectsGrid True if it belongs to the object grid. 
     * @return Component that contains the object actions.
     */
    private HorizontalLayout createObjectAction(BusinessObjectLight sourceObject, BusinessObjectLight targetObject,
            boolean release, boolean reports, boolean isObjectsGrid) {  
        HorizontalLayout lytAction = new HorizontalLayout();
        lytAction.setSpacing(false);
        lytAction.setSizeFull();
        
        ActionButton btnGoToDashboard = new ActionButton(new ActionIcon(VaadinIcon.ARROW_FORWARD),
                ts.getTranslatedString("module.navigation.widgets.object-dashboard.open-to-dashboard"));
        btnGoToDashboard.addClickListener(event -> {
            getUI().ifPresent(ui -> {
                ui.getSession().setAttribute(BusinessObjectLight.class, sourceObject);
                ui.getPage().open(RouteConfiguration.forRegistry(VaadinService.getCurrent().getRouter().getRegistry()).getUrl(ObjectDashboard.class), "_blank");
            });
        });
        lytAction.add(btnGoToDashboard);
        
        ActionButton btnShowInfo = new ActionButton(new ActionIcon(VaadinIcon.INFO_CIRCLE)
                ,ts.getTranslatedString("module.navigation.actions.show-more-information-button-name"));
        btnShowInfo.addClickListener(event ->
            this.windowMoreInformation.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter(EActionParameter.OBJECT.getPropertyValue(), sourceObject))).open()
        );
        lytAction.add(btnShowInfo);
        
        Command releaseRelationship = () -> {
            if (isObjectsGrid) {
                createDataProvider(new SoftwareObjectNode(targetObject), null);
                lytDetails.removeAll();
            } else {
                loadDetails(targetObject);
                gridObjects.getDataProvider().refreshAll();
            }
        };
        if (release && targetObject != null) {
            ActionButton btnRelease = new ActionButton(new ActionIcon(VaadinIcon.UNLINK),
                    String.format(this.releaseRelationshipVisualAction.getModuleAction().getDisplayName(),
                            sourceObject.getName(), targetObject.getName()));
            btnRelease.addClickListener(event -> {
                this.releaseRelationshipVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(EActionParameter.SOURCE_OBJECT.getPropertyValue(), sourceObject),
                        new ModuleActionParameter(EActionParameter.TARGET_OBJECT.getPropertyValue(), targetObject),
                        new ModuleActionParameter(EActionParameter.RELEASE_RELATIONSHIP.getPropertyValue(), releaseRelationship)
                )).open();
            });
            lytAction.add(btnRelease);
        }
         
        if (reports) {
             //Used to show reports from an object or license
            ActionButton btnReports = new ActionButton(new ActionIcon(VaadinIcon.FILE_TABLE), this.launchClassLevelReportAction.getName());
            btnReports.setEnabled(true);
            btnReports.addClickListener(event ->
                this.launchClassLevelReportAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(EActionParameter.BUSINESS_OBJECT.getPropertyValue(), sourceObject))).open()
            );
            lytAction.add(btnReports);
        }
        return lytAction;
    }
    
    private void clearElements() {
        lytCenter.removeAll();
        lytDetails.removeAll();
    }
}