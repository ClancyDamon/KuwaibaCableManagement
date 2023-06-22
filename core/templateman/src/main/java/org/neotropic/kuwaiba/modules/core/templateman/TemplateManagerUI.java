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
package org.neotropic.kuwaiba.modules.core.templateman;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBox.ItemFilter;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.templateman.actions.DeleteTemplateItemVisualAction;
import org.neotropic.kuwaiba.modules.core.templateman.actions.DeleteTemplateSubItemVisualAction;
import org.neotropic.kuwaiba.modules.core.templateman.actions.DeleteTemplateVisualAction;
import org.neotropic.kuwaiba.modules.core.templateman.actions.NewBulkTemplateItemVisualAction;
import org.neotropic.kuwaiba.modules.core.templateman.actions.NewBulkTemplateSpecialItemVisualAction;
import org.neotropic.kuwaiba.modules.core.templateman.actions.NewTemplateItemVisualAction;
import org.neotropic.kuwaiba.modules.core.templateman.actions.NewTemplateSpecialItemVisualAction;
import org.neotropic.kuwaiba.modules.core.templateman.actions.NewTemplateVisualAction;
import org.neotropic.kuwaiba.modules.core.templateman.visual.TemplateNode;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.menu.FloatMenu;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.tree.NavTreeGrid;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Main UI for template manager module, initialize all display elements and business logic.
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route(value = "templates", layout = TemplateManagerLayout.class)
public class TemplateManagerUI extends FlexLayout implements ActionCompletedListener, 
        PropertySheet.IPropertyValueChangedListener, HasDynamicTitle, AbstractUI {
    /**
     * combo box to list class attributes
     */
    private final ComboBox<ClassMetadataLight> cmbAllClasses;
    /**
     * grid to list class attributes
     */
    private final Grid<TemplateObjectLight> grdTemplates;
    /**
     * Object to create new template
     */
    private ActionButton btnAddTemplate;
    /**
     * data provider use for lazy loading and filtering, belongs to gridTemplates
     */
    private ListDataProvider<TemplateObjectLight> templatesDataProvider;
    /**
     * Grid to list class attributes
     */
    private NavTreeGrid<TemplateNode> navTemplate;
    /**
     * the visual action to delete a list type item
     */
    private final VerticalLayout lytvLeftSearchClasses;
    /**
     * Grid for gridChildTemplate and it options
     */
    private final VerticalLayout lytvCenterTemplateEditor;
    /**
     * The header of the template editor holds the actions
     */
    private final VerticalLayout lythTemplateEditorHeader;
    /**
     * The wraps the template Grid editor
     */
    private final VerticalLayout lytvTemplateEditorContent;
    /**
     * the visual action to delete a list type item
     */
    private final VerticalLayout lytvRigthPropertySheet;
    /**
     * Properties by any element selected
     */
    private PropertySheet propertysheet;
    /**
     * Properties header by element selected
     */
    private Label headerPropertySheet;
    /**
     * refresh template child grid action
     */
    private Command refreshChildAction;
    /**
     * Class selected
     */
    private ClassMetadataLight selectedClass;
    /**
     * Template selected
     */
    private TemplateObjectLight selectedTemplate;
    /**
     * Template item selected
     */
    private TemplateObjectLight selectedTemplateItem;
    /**
     * Property to edit is for child
     */
    private boolean editChild;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * translation service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Delete a template visual action
     */
    @Autowired
    private DeleteTemplateVisualAction deleteTemplateVisualAction;
    /**
     * Create a new Template visual
     */
    @Autowired
    private NewTemplateVisualAction newTemplateVisualAction;
    /**
     * Delete a template visual action
     */
    @Autowired
    private DeleteTemplateItemVisualAction deleteTemplateItemVisualAction;
    /**
     * Create a new Template visual
     */
    @Autowired
    private NewTemplateItemVisualAction newTemplateItemVisualAction;
    /**
     * Create bulk Templates
     */
    @Autowired
    private NewBulkTemplateItemVisualAction newBulkTemplateItemVisualAction;
    /**
     * Delete a template visual action
     */
    @Autowired
    private DeleteTemplateSubItemVisualAction deleteTemplateSubItemVisualAction;
    /**
     * Create a new Template visual
     */
    @Autowired
    private NewTemplateSpecialItemVisualAction newTemplateSpecialItemVisualAction;
    /**
     * Create a new Template visual
     */
    @Autowired
    private NewBulkTemplateSpecialItemVisualAction newBulkTemplateSpecialItemVisualAction;
    /**
     * Factory to build resources from data source.
     */
    @Autowired
    private ResourceFactory resourceFactory;
    
    public TemplateManagerUI() {
        super();
        //init principal elements
        this.cmbAllClasses = new ComboBox<>();
        this.grdTemplates = new Grid<>();
        this.lytvLeftSearchClasses = new VerticalLayout();
        this.lytvCenterTemplateEditor = new VerticalLayout();
        this.lythTemplateEditorHeader = new VerticalLayout();
        this.lytvTemplateEditorContent = new VerticalLayout();
        this.lytvRigthPropertySheet = new VerticalLayout();
    }

    /**
     * Fired when a dialog or other action finish
     * @param ev action of any action is complete
     */
    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS &&  ev.getActionResponse() != null){
            try{
                if(ev.getActionResponse().containsKey(NewTemplateVisualAction.PARAM_CLASS_METADATA))
                    updateTemplatesGrid((ClassMetadataLight)ev.getActionResponse().get(NewTemplateVisualAction.PARAM_CLASS_METADATA));
                else if(navTemplate != null && ev.getActionResponse().containsKey(Constants.PROPERTY_ID)){ //we only add/remove nodes if the tree is shown
                    Optional<TemplateNode> affectedNode = navTemplate.findNodeById((String)ev.getActionResponse().get(Constants.PROPERTY_ID)); 

                    affectedNode.ifPresent(n ->{
                        if(ev.getActionResponse().containsKey(ActionResponse.ActionType.ADD))
                            navTemplate.update(n);

                        else if(ev.getActionResponse().containsKey(ActionResponse.ActionType.REMOVE)) {
                            navTemplate.remove(n);
                            updatePropertySheet(selectedClass);
                        }
                        else if (ev.getActionResponse().containsKey(ActionResponse.ActionType.MOVE))
                            navTemplate.moveNode(navTemplate.findNodeById(((TemplateObjectLight) ev.getActionResponse().get(ActionResponse.ActionType.MOVE)).getId()).orElse(null), n);

                        else if (ev.getActionResponse().containsKey(ActionResponse.ActionType.COPY))
                            navTemplate.copyNode(navTemplate.findNodeById(((TemplateObjectLight) ev.getActionResponse().get(ActionResponse.ActionType.COPY)).getId()).orElse(null));
                    });
                    
                    if(!affectedNode.isPresent())
                        updateTemplateGridEditor(selectedTemplate);
                } else if(navTemplate == null && ev.getActionResponse().containsKey(Constants.PROPERTY_ID))
                    loadNavTemplateEditor(selectedTemplate);

                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                            AbstractNotification.NotificationType.INFO, ts).open();
            } catch (NoSuchElementException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
        else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
    }

    /**
     * Creates the left layout that contains the class metadata selector and the 
     * templates of the selected class and the new template button
     */
    private void buildLeftLayout() {
        this.lytvLeftSearchClasses.setPadding(true);
        this.lytvLeftSearchClasses.setSpacing(true);
        try {
            HorizontalLayout lythTemplateOptions = new HorizontalLayout();
            //elements properties            
            cmbAllClasses.setWidthFull();
            cmbAllClasses.setAutofocus(true);
            cmbAllClasses.setPlaceholder(ts.getTranslatedString("module.templateman.filter-class"));
            cmbAllClasses.setClearButtonVisible(true);
            cmbAllClasses.addValueChangeListener(event -> {
                if (event.getValue() != null) {
                    selectedClass = event.getValue();
                    editChild = false;
                    updateTemplatesGrid(event.getValue());
                    updateTemplateGridEditor(null);
                    updatePropertySheet(event.getValue());
                } else
                    btnAddTemplate.setEnabled(false);
            });
            
            this.lytvLeftSearchClasses.add(cmbAllClasses, lythTemplateOptions);
            //define listeners and data providers
            buildTemplatesGrid();
            buildClassesItemsProvider();
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.unexpected-error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    /**
     * Create data provider for left grid
     * @throws MetadataObjectNotFoundException; not found or invalid query search
     */
    private void buildClassesItemsProvider() throws MetadataObjectNotFoundException, InvalidArgumentException {
        List<ClassMetadataLight> allClassesLight = mem.getSubClassesLight(Constants.CLASS_INVENTORYOBJECT, false, false);
        //custom item name filter on the objects rendered in the combobox:
        ItemFilter<ClassMetadataLight> filter = (element, filterString) -> element
                .getName().toUpperCase().startsWith(filterString.toUpperCase());
        this.cmbAllClasses.setItems(filter, allClassesLight);
        this.cmbAllClasses.setItemLabelGenerator(ClassMetadataLight::getName);
    }

    /**
     * Create display form and set action listeners
     */
    private void buildTemplatesGrid() {
        //build template options
        btnAddTemplate = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O)
                , this.newTemplateVisualAction.getModuleAction().getDisplayName());
        //set element properties
        btnAddTemplate.addClickListener(clickEvent -> {
            this.newTemplateVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter(NewTemplateVisualAction.PARAM_CLASS_METADATA, selectedClass)
            )).open();
        });
        btnAddTemplate.setHeight("32px");
        //build template table 
        VerticalLayout grdLayout = new VerticalLayout();
        grdLayout.setPadding(false);
        grdTemplates.setSelectionMode(Grid.SelectionMode.SINGLE);
        grdTemplates.setClassName("upload-grid");
        grdTemplates.addColumn(TemplateRenderer.<TemplateObjectLight>of(
                "<div style = \"white-space: normal; overflow-wrap: anywhere;\">"
                        + "[[item.name]] &nbsp; <font class=\"text-secondary\">[[item.classToBeApplied]]</font>"
                        + "</div>")
                .withProperty("name", TemplateObjectLight::getName)
                .withProperty("classToBeApplied", TemplateObjectLight::getClassName))
                .setKey(ts.getTranslatedString("module.general.labels.name"));
        grdTemplates.addComponentColumn(this::createTemplateActions)
                .setTextAlign(ColumnTextAlign.END).setWidth("90px").setFlexGrow(0);

        grdTemplates.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                this.selectedTemplate = event.getValue();
                updateTemplateGridEditor(event.getValue());
            } else {
                if (event.isFromClient()) 
                    this.selectedTemplate = null;
            }
        });

        createTemplatesGridFilter(grdLayout);
        // Title
        Label templateTitle = new Label(ts.getTranslatedString("module.templateman.templates"));
        HorizontalLayout lytLeftHeader = new HorizontalLayout(templateTitle);
        lytLeftHeader.setClassName("templates-left-header");
        lytLeftHeader.setSpacing(false);
        // Left Action Buttons
        HorizontalLayout lytLeftActionButtons = new HorizontalLayout(btnAddTemplate);
        lytLeftActionButtons.setPadding(false);
        lytLeftActionButtons.setMargin(false);
        lytLeftActionButtons.setSpacing(false);
        lytLeftActionButtons.setId("right-actions-lyt");
        lytLeftActionButtons.setJustifyContentMode(JustifyContentMode.END);
        lytLeftActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        // Left controls
        HorizontalLayout lytLeftControls = new HorizontalLayout(lytLeftHeader, lytLeftActionButtons);
        lytLeftControls.setClassName("script-control");
        lytLeftControls.setPadding(false);
        lytLeftControls.setMargin(false);
        lytLeftControls.setSpacing(true);
        lytLeftControls.setWidthFull();
        lytLeftControls.setJustifyContentMode(JustifyContentMode.CENTER);
        lytLeftControls.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        // Add content to main layout
        this.lytvLeftSearchClasses.add(lytLeftControls, grdLayout);
    }
    
    /**
     * Create a filter on the template grid on the left side of the page.
     * 
     * @param grdLayout grid container
     */
    private void createTemplatesGridFilter(VerticalLayout grdLayout) {
        TextField txtFilterListTypeName = new TextField();
        txtFilterListTypeName.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));
        //properties        
        txtFilterListTypeName.setValueChangeMode(ValueChangeMode.EAGER);
        txtFilterListTypeName.setSuffixComponent(new ActionIcon(VaadinIcon.SEARCH
                , ts.getTranslatedString("module.templateman.filter-template")));
        txtFilterListTypeName.setWidthFull();
        txtFilterListTypeName.addValueChangeListener(event -> {
            if (templatesDataProvider != null) {
                templatesDataProvider.addFilter(
                        element -> StringUtils.containsIgnoreCase(element.getName(),
                                txtFilterListTypeName.getValue()));
            }
        });
        grdLayout.add(txtFilterListTypeName);
        grdLayout.add(grdTemplates);
    }

    /**
     * Options over a template, add special, add non-special and remove template
     * @param template selected template
     * @return menu of options
     */
    private MenuBar createTemplateActions(TemplateObjectLight template) {
        // Layout for actions
        //Menu for grid Child Template in case need add new item or special item and remove
        MenuBar mnuAddChildTemplateItems = new MenuBar();
        //elements properties                
        //mnuAddChildTemplateItems.setThemeName("items-option-menu");
        mnuAddChildTemplateItems.setWidthFull();
        mnuAddChildTemplateItems.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        //mnuAddChildTemplateItems.setClassName("menu-templateitem-options");
        //Menu to remove template item
        MenuItem removeTemplate = createIconItem(mnuAddChildTemplateItems, VaadinIcon.TRASH,
                this.deleteTemplateVisualAction.getModuleAction().getDisplayName());
        removeTemplate.getElement().getThemeList().add("BUTTON_SMALL");
        removeTemplate.addClickListener( e ->{
            Command deleteTemplateAction = () -> {//refresh template grid and refresh property sheet
                loadTemplatesGridData(selectedClass);
                updatePropertySheet(selectedClass);
                updateTemplateGridEditor(null);
            };
            this.deleteTemplateVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("templateItem", template),
                    new ModuleActionParameter("commandClose", deleteTemplateAction)
            )).open();
        });
        //add template item
        //Sub menu for option to add item and multiple item
        MenuItem mnuAddTemplateChildren = createIconItem(mnuAddChildTemplateItems, VaadinIcon.PLUS_SQUARE_O,
                this.newTemplateItemVisualAction.getModuleAction().getDisplayName());
        mnuAddTemplateChildren.getElement().getThemeList().add("BUTTON_SMALL");
        mnuAddTemplateChildren.getSubMenu()
                .addItem(ts.getTranslatedString("module.templateman.actions.new-template-item-sigle.name"),
                        e -> {
                            this.selectedTemplate = template;
                            this.newTemplateItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                                    new ModuleActionParameter("parentClassName", template.getClassName()),
                                    new ModuleActionParameter("parentId", template.getId()),
                                    new ModuleActionParameter("commandClose", refreshChildAction)
                            )).open();
                        }
                );
        mnuAddTemplateChildren.getSubMenu()
                .addItem(ts.getTranslatedString("module.templateman.actions.new-template-item-multiple.name"),
                        e -> {
                            this.selectedTemplate = template;
                            this.newBulkTemplateItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                                    new ModuleActionParameter("parentClassName", template.getClassName()),
                                    new ModuleActionParameter("parentId", template.getId()),
                                    new ModuleActionParameter("commandClose", refreshChildAction)
                            )).open();
                        }
                );
        //add template special item
        //Sub menu for option to add special item and multiple special item
        MenuItem mnuAddTemplateSpecialChildren = createIconItem(mnuAddChildTemplateItems, VaadinIcon.ASTERISK,
                this.newTemplateSpecialItemVisualAction.getModuleAction().getDisplayName());
        mnuAddTemplateSpecialChildren.getElement().getThemeList().add("BUTTON_SMALL");
        mnuAddTemplateSpecialChildren.getSubMenu()
                .addItem(ts.getTranslatedString("module.templateman.actions.new-template-item-sigle.name"),
                        e -> {
                            this.selectedTemplate = template;
                            this.newTemplateSpecialItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                                    new ModuleActionParameter("parentClassName", template.getClassName()),
                                    new ModuleActionParameter("parentId", template.getId()),
                                    new ModuleActionParameter("commandClose", refreshChildAction)
                            )).open();
                        }
                );
        mnuAddTemplateSpecialChildren.getSubMenu()
                .addItem(ts.getTranslatedString("module.templateman.actions.new-template-item-multiple.name"),
                        e -> {
                            this.selectedTemplate = template;
                            this.newBulkTemplateSpecialItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                                    new ModuleActionParameter("parentClassName", template.getClassName()),
                                    new ModuleActionParameter("parentId", template.getId()),
                                    new ModuleActionParameter("commandClose", refreshChildAction)
                            )).open();
                        }
                );
        return mnuAddChildTemplateItems;
    }

    private MenuItem createIconItem(MenuBar menu, VaadinIcon iconName, String ariaLabel) {
        Icon icon = new Icon(iconName);
        MenuItem item = menu.addItem(icon);
        item.getElement().setAttribute("title", ariaLabel);
        return item;
    }
    /**
     * Create data provider for principal grid search
     */
    private void updateTemplatesGrid(ClassMetadataLight classMetadata) {
        if (classMetadata != null) {
            cmbAllClasses.setValue(classMetadata);
            loadTemplatesGridData(classMetadata);
            btnAddTemplate.setEnabled(true);
        } else {
            grdTemplates.setItems(new ArrayList<>());
            btnAddTemplate.setEnabled(false);
        }
    }

    /**
     * Create display form and set action listeners
     */
    private void buildTemplateEditorHeader() {
        this.lytvCenterTemplateEditor.setPadding(true);
        this.lytvCenterTemplateEditor.setSpacing(true);
        // Header
        Label templateStructureTitle = new Label(ts.getTranslatedString("module.templateman.items"));
        templateStructureTitle.setClassName("templates-left-header");
        this.lythTemplateEditorHeader.add(templateStructureTitle);
        this.lythTemplateEditorHeader.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, templateStructureTitle);
    }
   
    /**
     * Creates data provider for principal grid
     * search
     */
    private void updateTemplateGridEditor(TemplateObjectLight template) {
        lytvTemplateEditorContent.removeAll();
        if (template != null) {
            selectedTemplate = template;
            loadNavTemplateEditor(template);
            updatePropertySheet(template);
        }
    }

    /**
     * Create display property sheet
     */
    private void buildPropertySheetLayout() {
        headerPropertySheet = new Label(
                String.format(ts.getTranslatedString("module.templateman.propertysheet.labels.header")
                , selectedClass != null ? selectedClass.getName() : ""));
        headerPropertySheet.setClassName("templates-properties-header");
        
        propertysheet = new PropertySheet(ts, new ArrayList<>());
        lytvRigthPropertySheet.setSpacing(true);
        lytvRigthPropertySheet.setPadding(true);
        lytvRigthPropertySheet.add(headerPropertySheet, propertysheet);
        propertysheet.addPropertyValueChangedListener(this);
    }

    /**
     * update property sheet in case element selected is a class
     * @param object;ClassMetadataLight; parent class element
     */
    private void updatePropertySheet(ClassMetadataLight object) {
        try {
            ClassMetadata objectFull = mem.getClass(object.getId());
            if (objectFull != null) {
                headerPropertySheet.setText(
                        String.format(ts.getTranslatedString("module.templateman.propertysheet.labels.header"),
                                 objectFull.getName()));
                propertysheet.setItems(PropertyFactory.generalPropertiesFromClass(objectFull, ts));
                propertysheet.setReadOnly(true);
            }
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }

    }

    /**
     * update property sheet in case element selected is a template
     * @param object;ClassMetadataLight; parent class element
     */
    private void updatePropertySheet(TemplateObjectLight object) {
        try {
            if(object != null) {
                TemplateObject objectFull = aem.getTemplateElement(object.getClassName(), object.getId());
                HashMap<String, Object> attributes = new HashMap<>(objectFull.getAttributes());
                BusinessObject businessObject = new BusinessObject(objectFull.getClassName(), objectFull.getId(), objectFull.getName(), attributes);
                headerPropertySheet.setText(String.format(ts.getTranslatedString("module.templateman.propertysheet.labels.header"),
                        objectFull.getName()));
                propertysheet.setItems(PropertyFactory.propertiesFromBusinessObject(businessObject, ts, aem, mem));
                propertysheet.setReadOnly(false);
            } else {
                propertysheet.setItems(new ArrayList<>());
                propertysheet.setReadOnly(false);
            }
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }

    }

    /**
     * Creates a new Structure item, based in allowed content.
     * @param se;SelectionEvent; event of click in expandable item grid
     */
    private void editStructureItem(SelectionEvent<Grid<TemplateNode>, TemplateNode> se) {
        se.getFirstSelectedItem().ifPresent( n -> {
                editChild = true;
                selectedTemplateItem = (TemplateObjectLight)n.getObject();
                updatePropertySheet(selectedTemplateItem);
            });
    }

    /**
     * Populates templates grid data provider and refresh items
     * @param classMetadata; ClassMetadataLight; parent class
     */
    private void loadTemplatesGridData(ClassMetadataLight classMetadata) {
        try {
            List<TemplateObjectLight> allTemplatesLight = aem.getTemplatesForClass(classMetadata.getName());
            this.templatesDataProvider = new ListDataProvider<>(allTemplatesLight);
            grdTemplates.setDataProvider(templatesDataProvider);
            this.templatesDataProvider.refreshAll();            
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    /**
     * Create template data provider items (structure )and refresh items
     * @param template; TemplateObjectLight; parent template
     */
    private void loadNavTemplateEditor(TemplateObjectLight template) {
        lytvTemplateEditorContent.removeAll();
        this.navTemplate = new NavTreeGrid<TemplateNode>() {
            @Override
            public List<TemplateNode> fetchData(TemplateNode node) {
                List<TemplateObjectLight> templateChildren = new ArrayList<>();
                if (template != null) {
                    templateChildren.addAll(aem.getTemplateElementChildren(node.getClassName(), node.getId()));
                    templateChildren.addAll(aem.getTemplateSpecialElementChildren(node.getClassName(), node.getId()));
                }
                return templateChildren.stream()
                        .map(child -> new TemplateNode(child, resourceFactory))
                        .collect(Collectors.toList());
            }
        };
        navTemplate.createDataProvider(new TemplateNode(template, resourceFactory));
        
        navTemplate.addComponentHierarchyColumn(TemplateNode::render);
        navTemplate.addComponentColumn(this::buildTemplateNodeMenuActions)
              .setTextAlign(ColumnTextAlign.CENTER).setFlexGrow(0).setWidth("90px");
        navTemplate.addSelectionListener(this::editStructureItem);
        lytvTemplateEditorContent.add(navTemplate);
    }
    

    /**
     * Creates option for any member inside tree grid
     * @param templateNode;TemplateObjectLight; inline element
     * @return lythOptions;Component; Horizontal layout with option
     */
    private Component buildTemplateNodeMenuActions(TemplateNode templateNode) {
        FloatMenu menu = new FloatMenu(templateNode.getId());
        lytvTemplateEditorContent.add(menu); //we must add the paper dialog to the main layout
        menu.addMenuItem(newTemplateItemVisualAction.getTitle(),
                e -> {
                    newTemplateItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter(Constants.PROPERTY_PARENT_CLASS_NAME, templateNode.getClassName()),
                    new ModuleActionParameter(Constants.PROPERTY_PARENT_ID, templateNode.getId())
                )).open();
                menu.close();
            });        

        menu.addMenuItem(newBulkTemplateItemVisualAction.getTitle(),
                e -> { 
                    this.newBulkTemplateItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter(Constants.PROPERTY_PARENT_CLASS_NAME, templateNode.getClassName()),
                    new ModuleActionParameter(Constants.PROPERTY_PARENT_ID, templateNode.getId())
                )).open();
                menu.close();
            }); 
        
        menu.addMenuItem(newTemplateSpecialItemVisualAction.getTitle(),
                e -> { 
                    this.newTemplateSpecialItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(Constants.PROPERTY_PARENT_CLASS_NAME, templateNode.getClassName()),
                        new ModuleActionParameter(Constants.PROPERTY_PARENT_ID, templateNode.getId())
                )).open();
                menu.close();
            }); 
        
        menu.addMenuItem(newBulkTemplateSpecialItemVisualAction.getTitle(),  
                e -> { 
                    this.newBulkTemplateSpecialItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(Constants.PROPERTY_PARENT_CLASS_NAME, templateNode.getClassName()),
                        new ModuleActionParameter(Constants.PROPERTY_PARENT_ID, templateNode.getId())
                )).open();
                menu.close();
            });
        
        menu.addMenuItem(deleteTemplateItemVisualAction.getTitle(),
                e -> { 
                    this.deleteTemplateItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter(Constants.PROPERTY_CLASSNAME, templateNode.getClassName()),
                    new ModuleActionParameter(Constants.PROPERTY_ID, templateNode.getId())
                )).open();
            menu.close();
        }); 
        
        return menu.getBtnOpen();
    }
    
    @Override
    public void updatePropertyChanged(AbstractProperty property) {
        try {
            String[] attributesNames = {property.getName()};
            String[] attributesValues = {PropertyValueConverter.getAsStringToPersist(property)};
            if (!editChild) {//update first containment child               
                aem.updateTemplateElement(selectedTemplate.getClassName(), selectedTemplate.getId(),
                        attributesNames, attributesValues);
                loadTemplatesGridData(selectedClass);
                grdTemplates.asSingleSelect().setValue(selectedTemplate);
            } else {//update lower containment child
                aem.updateTemplateElement(selectedTemplateItem.getClassName(), selectedTemplateItem.getId(),
                        attributesNames, attributesValues);
                loadNavTemplateEditor(selectedTemplate);
                //grdTemplate.asSingleSelect().setValue(selectedTemplateItem);
            }
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), 
                    ts.getTranslatedString("module.general.messages.property-updated-successfully"), 
                    AbstractNotification.NotificationType.INFO, ts).open();
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                    AbstractNotification.NotificationType.ERROR, ts).open();
            propertysheet.undoLastEdit();
        }
    }

    @Override
    public void onDetach(DetachEvent ev) {
        this.newTemplateVisualAction.unregisterListener(this);
        this.newBulkTemplateItemVisualAction.unregisterListener(this);
        this.deleteTemplateVisualAction.unregisterListener(this);
        this.newTemplateItemVisualAction.unregisterListener(this);
        this.deleteTemplateItemVisualAction.unregisterListener(this);
        this.newTemplateSpecialItemVisualAction.unregisterListener(this);
        this.newBulkTemplateSpecialItemVisualAction.unregisterListener(this);
        this.deleteTemplateSubItemVisualAction.unregisterListener(this);
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.templateman.title");
    }

    @Override
    public void initContent() {
        setSizeFull();
        //create action for close dialog
        refreshChildAction = () -> loadNavTemplateEditor(selectedTemplate);
        //register visual actions       
        this.newTemplateVisualAction.registerActionCompletedLister(this);
        this.deleteTemplateVisualAction.registerActionCompletedLister(this);
        this.newTemplateItemVisualAction.registerActionCompletedLister(this);
        this.deleteTemplateItemVisualAction.registerActionCompletedLister(this);
        this.newBulkTemplateItemVisualAction.registerActionCompletedLister(this);
        this.deleteTemplateSubItemVisualAction.registerActionCompletedLister(this);
        this.newTemplateSpecialItemVisualAction.registerActionCompletedLister(this);
        this.newBulkTemplateSpecialItemVisualAction.registerActionCompletedLister(this);
        //set elements properties
        buildLeftLayout();
        buildTemplateEditorHeader();
        buildPropertySheetLayout();
        //define layout         
        this.lytvLeftSearchClasses.setWidth("30%");
        this.lytvCenterTemplateEditor.setWidth("65%");
        this.lytvRigthPropertySheet.setWidth("35%");
        lytvCenterTemplateEditor.add(lythTemplateEditorHeader, lytvTemplateEditorContent);
        add(lytvLeftSearchClasses, lytvCenterTemplateEditor, lytvRigthPropertySheet);
    }
}