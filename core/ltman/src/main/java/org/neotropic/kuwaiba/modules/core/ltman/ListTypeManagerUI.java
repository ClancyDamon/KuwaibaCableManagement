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

package org.neotropic.kuwaiba.modules.core.ltman;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import elemental.json.Json;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.ltman.actions.DeleteListTypeItemVisualAction;
import org.neotropic.kuwaiba.modules.core.ltman.actions.NewListTypeItemVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.ShowMoreInformationAction;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the List type manager module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route(value = "ltman", layout = ListTypeManagerLayout.class)
public class ListTypeManagerUI extends VerticalLayout implements ActionCompletedListener, 
        PropertySheet.IPropertyValueChangedListener, HasDynamicTitle, AbstractUI {

    /**
     * the visual action to create a new list type item
     */
    @Autowired
    private NewListTypeItemVisualAction newListTypeItemVisualAction;

    @Autowired
    private TranslationService ts;
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
     * The grid with the list Types
     */
    private final Grid<ClassMetadataLight> tblListTypes;
    /**
     * The grid with the list Type items
     */
    private Grid<BusinessObjectLight> tblListTypeItems;
    /**
     * The grid with the attached files
     */
    private Grid<FileObjectLight> grdFiles;
     /**
     * Label displayed when no files has been attached
     */
    private Label lblNoFiles;
    /**
     * object to save the selected list type
     */
    private ClassMetadataLight currentListType;
    /**
     * object to save the selected list type item
     */
    private BusinessObjectLight currentListTypeItem;
    /**
     * the top horizontal layout
     */
    private HorizontalLayout lytTop;
    /**
     * the top horizontal layout left side holds the new list type item
     */
    private HorizontalLayout lytButton;
    /**
     * the top horizontal layout center side holds the current selected list type item
     */
    private HorizontalLayout lytCurrentSelectedTitle;
    /**
     * the top horizontal layout right side holds the current selected list type item
     */
    private HorizontalLayout lytPropertiesTitle;
    /**
     * first column the list types
     */
    private VerticalLayout lytListTypes;
    /**
     * Second column the list of list type items
     */
    private VerticalLayout lytListTypeItems;
    /**
     * third column details of the list type item
     */
    private VerticalLayout lytTabs;
    /**
     * the visual action to delete a list type item
     */ 
    private VerticalLayout lytPropertySheet;
            
    /**
     * The window to show more information about an object.
     */
    @Autowired
    private ShowMoreInformationAction windowMoreInformation;
            
    @Autowired
    private DeleteListTypeItemVisualAction deleteListTypeItemVisualAction;
    
    PropertySheet propertysheet;

    public ListTypeManagerUI() {
        super();
        tblListTypes = new Grid<>();
        setSizeFull();       
    }
    
    @Override
    public void onDetach(DetachEvent ev) {
        this.newListTypeItemVisualAction.unregisterListener(this);
        this.deleteListTypeItemVisualAction.unregisterListener(this);
    }
    
    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            try {
                if(ev.getActionResponse().containsKey(ActionResponse.ActionType.ADD)) {
                    String newId = (String) ev.getActionResponse().get(Constants.PROPERTY_ID);
                    ClassMetadataLight selectedListType = (ClassMetadataLight)ev.getActionResponse().get(NewListTypeItemVisualAction.PARAM_LIST_TYPE);

                    if (tblListTypes != null && newId != null && selectedListType != null) {
                        currentListType = selectedListType;
                        tblListTypes.select(currentListType);
                        loadListTypeItems(currentListType);
                        //Don't delete this, it would be useful as example if it is need to get all the data provider elements as list
                        //List<BusinessObjectLight> lista = tblListTypeItems.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
                        if(tblListTypeItems != null){
                            BusinessObjectLight createdObj = tblListTypeItems.getDataProvider().fetch(new Query<>())
                                .filter(x -> x.getId().equals(newId))
                                .findAny().get();
                            tblListTypeItems.select(createdObj);
                        }
                    }                 
                } else { // Removing lti
                    currentListTypeItem = null;
                    if (currentListType != null) {
                        loadListTypeItems(currentListType);
                        lytTabs.setVisible(false);
                        propertysheet.clear();
                    }
                }
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                                AbstractNotification.NotificationType.INFO, ts).open();
            } catch (InvalidArgumentException | MetadataObjectNotFoundException ex) {
                Logger.getLogger(ListTypeManagerUI.class.getName()).log(Level.SEVERE, null, ex);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            }
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
    }
    
    public void showActionCompledMessages(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) 
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                            AbstractNotification.NotificationType.INFO, ts).open();
        else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
    }

    private void loadFiles() {
        try {
            List<FileObjectLight> files = aem.getFilesForListTypeItem(currentListTypeItem.getClassName(), currentListTypeItem.getId());
            grdFiles.setItems(files);
            grdFiles.getDataProvider().refreshAll();
            lblNoFiles.setVisible(files.isEmpty());
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(ListTypeManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updatePropertySheet() {
        try {
            BusinessObject aWholeListTypeItem = aem.getListTypeItem(currentListTypeItem.getClassName(), currentListTypeItem.getId());
            propertysheet.setItems(PropertyFactory.propertiesFromBusinessObject(aWholeListTypeItem, ts, aem, mem));
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            Logger.getLogger(ListTypeManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    /**
     * The first column
     * @throws InvalidArgumentException 
     * @throws MetadataObjectNotFoundException 
     */
    private void buildListTypeGrid() throws InvalidArgumentException, MetadataObjectNotFoundException {
        // Build list type grid
        List<ClassMetadataLight> listTypes = mem.getSubClassesLight(Constants.CLASS_GENERICOBJECTLIST, false, false);
        ListDataProvider<ClassMetadataLight> dataProvider = new ListDataProvider<>(listTypes);
        tblListTypes.setSelectionMode(Grid.SelectionMode.SINGLE);
        tblListTypes.setDataProvider(dataProvider);
        tblListTypes.setHeightFull();
        Grid.Column<ClassMetadataLight> columnName = tblListTypes.addColumn(ClassMetadataLight::getName)
                .setKey(ts.getTranslatedString("module.general.labels.name"));
        
        tblListTypes.addSelectionListener(ev -> {
            try {
                if (ev.getFirstSelectedItem().isPresent()) {
                    propertysheet.clear();
                    lytTabs.setVisible(false);
                    currentListType = ev.getFirstSelectedItem().get();              
                    loadListTypeItems(ev.getFirstSelectedItem().get());
                }
            } catch (InvalidArgumentException | MetadataObjectNotFoundException ex) {
                Logger.getLogger(ListTypeManagerUI.class.getName()).log(Level.SEVERE, null, ex);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            }
        });
        
        HeaderRow filterRow = tblListTypes.appendHeaderRow();
        
        TextField txtFilterListTypeName = createTxtFieldListTypeName(dataProvider);
        filterRow.getCell(columnName).setComponent(txtFilterListTypeName);
        
        tblListTypes.appendFooterRow().getCell(columnName).setComponent(
            new Label(String.format("%s: %s: %s", ts.getTranslatedString("module.general.labels.total")
                    , listTypes.size() , ts.getTranslatedString("module.ltman.list-types"))));
    }
   
    private void loadListTypeItems(ClassMetadataLight item) throws MetadataObjectNotFoundException, InvalidArgumentException {
        lytListTypeItems.removeAll();
        lytCurrentSelectedTitle.removeAll();
        
        lytListTypeItems.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        tblListTypeItems = new Grid<>();
        tblListTypeItems.setSelectionMode(Grid.SelectionMode.SINGLE);
        //columns
        Grid.Column<BusinessObjectLight> nameColumn = tblListTypeItems.addColumn(BusinessObjectLight::getName).setAutoWidth(true);
        tblListTypeItems.addComponentColumn(i -> createListTypeItemActionGrid(i)).setTextAlign(ColumnTextAlign.END).setFlexGrow(0).setWidth("135px");
        //listners
        tblListTypeItems.addSelectionListener(ev -> {
            try {
                if (ev.getFirstSelectedItem().isPresent()) {
                    currentListTypeItem = ev.getFirstSelectedItem().get();
                    updatePropertySheet();
                    lytTabs.setVisible(true);
                    loadFiles();
                }
            } catch (Exception ex) {
                 new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, ts).open();
            }
        });
        //data
        List<BusinessObjectLight> listTypeItems = aem.getListTypeItems(item.getName());        
        tblListTypeItems.setItems(listTypeItems);
        tblListTypeItems.setHeightByRows(true);
        tblListTypeItems.getDataProvider().refreshAll();
        tblListTypeItems.appendFooterRow().getCell(nameColumn).setComponent(
            new Label(String.format("%s: %s: %s", ts.getTranslatedString("module.general.labels.total")
                    , listTypeItems.size() , ts.getTranslatedString("module.ltman.list-typeitems"))));

        Label lblCurrentSelectedHeader = new Label(item.getDisplayName().isEmpty() ? item.getName() : item.getDisplayName());
        lblCurrentSelectedHeader.setClassName("dialog-title");
        
        Icon iconMoreInfo = new Icon(VaadinIcon.INFO_CIRCLE_O);
        iconMoreInfo.setClassName("info-btn");
   
        Button btnInfo = new Button(iconMoreInfo, e -> 
                this.windowMoreInformation.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter("object", currentListTypeItem))).open());

        btnInfo.getElement().setProperty("title", this.windowMoreInformation.getDisplayName());
        btnInfo.setClassName("icon-button");
        
        lblCurrentSelectedHeader.getElement().getStyle().set("margin-left", "auto");
        lytCurrentSelectedTitle.add(lblCurrentSelectedHeader, btnInfo);
        lytListTypeItems.add(tblListTypeItems);
    }
    
    /**
     * Create a new input field to filter list types in the header row.
     * @param dataProvider Data provider to filter.
     * @return The new input field filter.
     */
    private TextField createTxtFieldListTypeName(ListDataProvider<ClassMetadataLight> dataProvider) {
        TextField txtListTypeName = new TextField();
        txtListTypeName.setSuffixComponent(new Icon(VaadinIcon.SEARCH));
        txtListTypeName.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtListTypeName.setValueChangeMode(ValueChangeMode.EAGER);
        txtListTypeName.setWidthFull();
        txtListTypeName.addValueChangeListener(e -> dataProvider.addFilter(
            project -> StringUtils.containsIgnoreCase(project.getName(),
                txtListTypeName.getValue())));
        return txtListTypeName;
    }

    private HorizontalLayout createListTypeItemActionGrid(BusinessObjectLight listTypeItem) {
        HorizontalLayout lyt; 
        
        Button btnUsages = new Button(ts.getTranslatedString("module.ltman.uses"),
                new Icon(VaadinIcon.SPLIT), e -> {
            try {
                List<BusinessObjectLight> listTypeItemUses = aem.getListTypeItemUses(listTypeItem.getClassName(), listTypeItem.getId(), -1);
                
                if (listTypeItemUses.isEmpty()) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.information"), ts.getTranslatedString("module.ltman.list-typeitem-not-used"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                } else {
                    ConfirmDialog dlgListTypeItemUses = new ConfirmDialog(ts, ts.getTranslatedString("module.ltman.list-typeitem-references"), "");
                    Grid<BusinessObjectLight> tblListTypeItemUses = new Grid<>();
                    tblListTypeItemUses.setClassName("width500px");
                    tblListTypeItemUses.setHeight("350px");
                    tblListTypeItemUses.setItems(listTypeItemUses);
                    tblListTypeItemUses.addColumn(BusinessObjectLight::getName).setHeader(ts.getTranslatedString("module.general.labels.name"));
                    tblListTypeItemUses.addColumn(BusinessObjectLight::getClassName).setHeader(ts.getTranslatedString("module.general.labels.class-name"));
                    
                    Button btnReleaseLTI = new Button(ts.getTranslatedString("module.ltman.release-usages"), new Icon(VaadinIcon.EXPAND), evt -> {
                        ConfirmDialog dlgConfirm = new ConfirmDialog(ts, ts.getTranslatedString("module.general.labels.confirmation"),
                                ts.getTranslatedString("module.ltman.confirm-release"),
                                ts.getTranslatedString("module.general.messages.ok"));
                        dlgConfirm.getBtnConfirm().addClickListener(l -> {
                            try {
                                aem.releaseListTypeItem(listTypeItem.getId());
                                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.ltman.lti-released"),
                                        AbstractNotification.NotificationType.INFO, ts).open();
                                dlgListTypeItemUses.close();
                                dlgConfirm.close();
                            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | OperationNotPermittedException | InvalidArgumentException | NotAuthorizedException ex) {
                                Logger.getLogger(ListTypeManagerUI.class.getName()).log(Level.SEVERE, null, ex);
                                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                                        AbstractNotification.NotificationType.ERROR, ts).open();
                                dlgConfirm.close();
                            }
                        });
                        dlgConfirm.open();
                    });
                    VerticalLayout lytContent = new VerticalLayout(tblListTypeItemUses, btnReleaseLTI); 
                    dlgListTypeItemUses.getBtnConfirm().setVisible(false);
                    dlgListTypeItemUses.add(lytContent);
                    dlgListTypeItemUses.open();
                }
            } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
                Logger.getLogger(ListTypeManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
                });
                        
        Button btnDelete = new Button(new Icon(VaadinIcon.TRASH), ev -> {
            this.deleteListTypeItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                                                                        new ModuleActionParameter("listTypeItem", listTypeItem))).open();
        });
        lyt = new HorizontalLayout(btnUsages, btnDelete);
        lyt.setWidth("130px");
        lyt.setSpacing(true);
        return lyt;
    }

    @Override
    public void updatePropertyChanged(AbstractProperty property) {
        try {
            if (currentListTypeItem != null) {
                
                HashMap<String, String> attributes = new HashMap<>();
                attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));

                aem.updateListTypeItem(currentListTypeItem.getClassName(), currentListTypeItem.getId(), attributes);

                loadListTypeItems(currentListType);
                tblListTypeItems.select(currentListTypeItem);

                updatePropertySheet();

                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                        AbstractNotification.NotificationType.INFO, ts).open();
            }
        } catch (MetadataObjectNotFoundException | ApplicationObjectNotFoundException
                 | InvalidArgumentException ex) {
            Logger.getLogger(ListTypeManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            propertysheet.undoLastEdit();
        }
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.ltman.title");
    }

    @Override
    public void initContent() {
        setSizeFull();
        lytButton = new HorizontalLayout();
        lytButton.setWidth("25%");
        lytButton.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        lytButton.setBoxSizing(BoxSizing.BORDER_BOX);
        
        lytCurrentSelectedTitle = new HorizontalLayout();
        lytCurrentSelectedTitle.setWidth("30%");
        lytCurrentSelectedTitle.getStyle().set("padding-left", "var(--lumo-space-m)");
        lytCurrentSelectedTitle.setBoxSizing(BoxSizing.BORDER_BOX);
        lytCurrentSelectedTitle.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        
        lytPropertiesTitle = new HorizontalLayout();
        lytPropertiesTitle.setWidth("45%");
        lytPropertiesTitle.getStyle().set("padding-left", "var(--lumo-space-m)");
        lytPropertiesTitle.setBoxSizing(BoxSizing.BORDER_BOX);
        lytPropertiesTitle.setDefaultVerticalComponentAlignment(Alignment.END);
        
        lytTop = new HorizontalLayout(lytButton, lytCurrentSelectedTitle, lytPropertiesTitle);
        lytTop.setWidthFull();
        lytTop.setMinHeight("25px");
        lytButton.getStyle().set("padding-left", "var(--lumo-space-m)");
        
        HorizontalLayout lytMainContent = new HorizontalLayout();
        lytMainContent.setSizeFull();
        
        try {
            this.newListTypeItemVisualAction.registerActionCompletedLister(this);
            this.deleteListTypeItemVisualAction.registerActionCompletedLister(this);

            Button btnAddListTypeItem = new Button(this.newListTypeItemVisualAction.getModuleAction().getDisplayName()
                    , new Icon(VaadinIcon.PLUS)
                    , e -> this.newListTypeItemVisualAction.getVisualComponent(
                            new ModuleActionParameterSet(new ModuleActionParameter(
                                            NewListTypeItemVisualAction.PARAM_LIST_TYPE
                                            , currentListType))).open()
            );
            btnAddListTypeItem.setClassName("nav-button");
            lytButton.add(btnAddListTypeItem);

            lytListTypes= new VerticalLayout(tblListTypes);
            lytListTypes.setWidth("25%");    
            lytListTypes.setSpacing(false);
            buildListTypeGrid();  

            Label lblSelectListType = new Label(ts.getTranslatedString("module.ltman.select-list-type"));
            lblSelectListType.setClassName("dialog-title");
            lytListTypeItems = new VerticalLayout(lblSelectListType); 
            lytListTypeItems.setHorizontalComponentAlignment(Alignment.CENTER, lblSelectListType);
            lytListTypeItems.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
            lytListTypeItems.setWidth("30%");
          
            propertysheet = new PropertySheet(ts, new ArrayList<>());
            propertysheet.addPropertyValueChangedListener(this);
            propertysheet.setHeightByRows(true);
            
            lytPropertySheet = new VerticalLayout(propertysheet);
            
            Tab tabPs = new Tab(ts.getTranslatedString("module.propertysheet.labels.header"));
            Div page1 = new Div();
            page1.setSizeFull();
            page1.add(lytPropertySheet);

            Tab tabFiles = new Tab(ts.getTranslatedString("module.navigation.actions.attach-file.name"));
            Div page2 = new Div();
            page2.add(createFilesTab());
            page2.setVisible(false);
        
            Map<Tab, Component> tabsToPages = new HashMap<>();
            tabsToPages.put(tabPs, page1);
            tabsToPages.put(tabFiles, page2);
            Tabs tabs = new Tabs(tabPs, tabFiles);
            Div pages = new Div(page1, page2);
            pages.setWidthFull();
            Set<Component> pagesShown = Stream.of(page1)
                    .collect(Collectors.toSet());

            tabs.addSelectedChangeListener(event -> {
                pagesShown.forEach(page -> page.setVisible(false));
                pagesShown.clear();
                Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
                selectedPage.setVisible(true);
                pagesShown.add(selectedPage);
            });
            
            lytPropertiesTitle.add(tabs);
            
            lytTabs = new VerticalLayout(pages);
            lytTabs.setWidth("45%");
            lytTabs.setSpacing(false);
            lytTabs.setPadding(false);
            
            lytMainContent.add(lytListTypes, lytListTypeItems, lytTabs);
        } catch (InvalidArgumentException | MetadataObjectNotFoundException ex){
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, ts).open();
        }
         
        add(lytTop, lytMainContent);
    }

    private Component createFilesTab() {
        MemoryBuffer bufferIcon = new MemoryBuffer();
        Upload uploadIcon = new Upload(bufferIcon);
        uploadIcon.setWidthFull();
        uploadIcon.setMaxFiles(1);
        uploadIcon.setDropLabel(new Label(ts.getTranslatedString("module.queries.dropmessage")));
        uploadIcon.addSucceededListener(event -> {
            try {
                byte [] imageData = IOUtils.toByteArray(bufferIcon.getInputStream());
                if (currentListTypeItem != null) { 
                    aem.attachFileToListTypeItem(event.getFileName(), "", imageData, 
                            currentListTypeItem.getClassName(), currentListTypeItem.getId());
                    loadFiles();
                    uploadIcon.getElement().setPropertyJson("files", Json.createArray());
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.queries.file-attached"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                }
            } catch (IOException | MetadataObjectNotFoundException | InvalidArgumentException | BusinessObjectNotFoundException | OperationNotPermittedException ex) {
                Logger.getLogger(ListTypeManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        uploadIcon.addFileRejectedListener(event -> {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), event.getErrorMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
        });
        
        lblNoFiles = new Label(ts.getTranslatedString("module.ltman.no-files-found-for-object"));
        lblNoFiles.setVisible(false);
        grdFiles = new Grid<>();
        grdFiles.addColumn(FileObjectLight::getName);
        grdFiles.addComponentColumn(item -> {
           Button btnDeleteFile = new Button(new Icon(VaadinIcon.TRASH), evt -> {
               
               ConfirmDialog dlgConfirm = new ConfirmDialog(ts, ts.getTranslatedString("module.general.labels.confirmation"),
                ts.getTranslatedString("module.general.labels.confirm-delete"),
                ts.getTranslatedString("module.queries.confirm-delete-file"));
               dlgConfirm.setWidth("400px");
               dlgConfirm.getBtnConfirm().addClickListener(listener -> {
                   try {
                       aem.detachFileFromListTypeItem( item.getFileOjectId(), currentListTypeItem.getClassName(), currentListTypeItem.getId());
                       loadFiles();
                       new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.queries.file-deleted"),
                               AbstractNotification.NotificationType.INFO, ts).open();
                       dlgConfirm.close();
                   } catch (BusinessObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
                       Logger.getLogger(ListTypeManagerUI.class.getName()).log(Level.SEVERE, null, ex);
                   }
               });
               dlgConfirm.open();            
           });
            btnDeleteFile.setClassName("icon-button");
            Anchor download = new Anchor();
            download.setId("anchorDownload");
            download.getElement().setAttribute("download", true);
            download.setClassName("hidden");
            download.getElement().setAttribute("visibility", "hidden");
            Button btnDownloadAnchor = new Button();
            btnDownloadAnchor.getElement().setAttribute("visibility", "hidden");
            Button btnDownload = new Button(new Icon(VaadinIcon.DOWNLOAD));
            btnDownload.setClassName("icon-button");
            btnDownload.addClickListener(evt -> {
               try {
                   FileObject fo = aem.getFile(item.getFileOjectId(), currentListTypeItem.getClassName(), currentListTypeItem.getId());
                   final StreamRegistration regn = VaadinSession.getCurrent().getResourceRegistry().
                           registerResource(createStreamResource(item.getName(), fo.getFile()));  
                   download.setHref(regn.getResourceUri().getPath());
                   btnDownloadAnchor.clickInClient();
               } catch (BusinessObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
                   Logger.getLogger(ListTypeManagerUI.class.getName()).log(Level.SEVERE, null, ex);
               }
            });
            download.add(btnDownloadAnchor);
           
           return new HorizontalLayout(btnDeleteFile, btnDownload, download);                   
        });
        
        VerticalLayout lytFiles = new VerticalLayout(uploadIcon, lblNoFiles, grdFiles);
        lytFiles.setSpacing(false);
        return lytFiles;
    }
    
    private StreamResource createStreamResource(String name, byte[] ba) {
        return new StreamResource(name, () -> new ByteArrayInputStream(ba));                                
    } 
}
