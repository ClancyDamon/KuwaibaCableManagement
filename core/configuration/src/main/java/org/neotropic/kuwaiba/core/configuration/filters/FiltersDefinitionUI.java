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
package org.neotropic.kuwaiba.core.configuration.filters;

import com.neotropic.flow.component.aceeditor.AceEditor;
import com.neotropic.flow.component.aceeditor.AceMode;
import com.neotropic.flow.component.paper.toggle.PaperToggleButton;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.FilterDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.configuration.ConfigurationManagerLayout;
import org.neotropic.kuwaiba.core.configuration.filters.actions.DeleteFilterDefinitionVisualAction;
import org.neotropic.kuwaiba.core.configuration.filters.actions.NewFilterDefinitionVisualAction;
import org.neotropic.kuwaiba.core.configuration.filters.actions.UpdateFilterDefinitionAction;
import org.neotropic.kuwaiba.core.configuration.filters.actions.UpdateFilterDefinitionVisualAction;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.grids.IconNameCellGrid;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.icons.ClassNameIconGenerator;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scripting.ScriptCompilationException;

/**
 * Main for the filters definition module.
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Route(value = "configuration/filters", layout = ConfigurationManagerLayout.class)
public class FiltersDefinitionUI extends VerticalLayout implements ActionCompletedListener, 
        HasDynamicTitle, AbstractUI {
    /**
     * Default width of the left side
     */
    private final static String DEFAULT_WIDTH = "100%";
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
     * Reference o the Translation Service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Factory to build resources from data source
     */  
    @Autowired
    private ResourceFactory resourceFactory;
    /**
     * The visual action to create a filter definition
     */
    @Autowired
    private NewFilterDefinitionVisualAction newFilterDefinitionVisualAction;
    /**
     * The visual action to delete a filter definition
     */
    @Autowired
    private DeleteFilterDefinitionVisualAction deleteFilterDefinitionVisualAction;
    /**
     * The action to update a filter definition
     */
    @Autowired
    private UpdateFilterDefinitionAction updateFilterDefinitionAction;
    /**
     * The visual action to update the filter action
     */
    @Autowired
    private UpdateFilterDefinitionVisualAction updateFilterDefinitionVisualAction;
    /**
     * Split the content
     */
    private SplitLayout splitLayout;
    /**
     * Contains the classes grid in the upper left side
     */
    private VerticalLayout lytClasses;
    /**
     * Contains the filters grid in the bottom left side
     */
    private VerticalLayout lytFiltersGrid;
    /**
     * Contains the script editor in the right side, bottom
     */
    private VerticalLayout lytScriptEditor;
    /**
     * Component to edit the script
     */
    private AceEditor aceEditorScript;
    /**
     * Combo box classes
     */
    private ComboBox<ClassMetadataLight> cmbClasses;
    /**
     * Grid filters of a given class metadata
     */
    private Grid<FilterDefinition> grdFilters;   
    /**
     * Static for label Filter: 
     */
    private Label lblFilter;
    /**
     * Title in the first line of the right side
     */
    private Label lblFilterName;
    /**
     * above the script editor
     */
    private HorizontalLayout lytScriptControls;
    /**
     * Buttons in the third line of the right side to manage filters
     */
    private ActionButton btnAddNewFilter;
    /**
     * Saves/compiles the script
     */
    private ActionButton btnSaveScriptChanges;
    /**
     * Saves the changes in the name field
     */
    private ActionButton btnEditFilterDefinition;
    /**
     * Deletes the filter
     */
    private ActionButton btnDeleteFilter;
    /**
     * Enables/disables the filter
     */
    private PaperToggleButton btnEnableFilter;
    /**
     * To keep track of the selected filter in the filters grid
     */
    private FilterDefinition lastFilterSelected;
    /**
     * input text box in the filters grid to search by filter definition name
     */
    private TextField txtSearchFitlerByName;
    /**
     * To use the filter and the provider in the grid
     */
    private ConfigurableFilterDataProvider<FilterDefinition, Void, FilterForFilterDefinition> dpConfigurableFilter;
     /**
     * The grid provider
     */
    private CallbackDataProvider<FilterDefinition, FilterForFilterDefinition> provider;
    /**
     * Wrappers a String to be sent as name filter to search Filters Definitions Grid
     */
    private FilterForFilterDefinition filterDefinitionNameToFilter;
    /**
     * Keeps the list of all classes
     */ 
    private List<ClassMetadataLight> classes;
    
    
    public FiltersDefinitionUI() {
        super();
        setSizeFull();
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.configman.filters.title");
    }
    
    @Override
    public void onDetach(DetachEvent ev) {
        this.newFilterDefinitionVisualAction.unregisterListener(this);
        this.updateFilterDefinitionVisualAction.unregisterListener(this);
        this.deleteFilterDefinitionVisualAction.unregisterListener(this);
    }

    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                        AbstractNotification.NotificationType.INFO, ts).open();
            if(ev.getActionResponse() != null && ev.getActionResponse().containsKey(ActionResponse.ActionType.UPDATE)){
                lblFilterName.setText(((String)ev.getActionResponse().get(Constants.PROPERTY_NAME)));
                grdFilters.getDataProvider().refreshAll();
            }
            else if(ev.getActionResponse() != null && ev.getActionResponse().containsKey(ActionResponse.ActionType.ADD) && ev.getActionResponse().get(Constants.PROPERTY_CLASSNAME) != null)
                setupFiltersGrid((String)ev.getActionResponse().get(Constants.PROPERTY_CLASSNAME));
            else if(ev.getActionResponse() != null && ev.getActionResponse().containsKey(ActionResponse.ActionType.REMOVE) && ev.getActionResponse().get(Constants.PROPERTY_CLASSNAME) != null)
                setupFiltersGrid((String)ev.getActionResponse().get(Constants.PROPERTY_CLASSNAME));
        } else 
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                AbstractNotification.NotificationType.ERROR, ts).open();
    }

    /**
     * Initialize the the with the all the metadata classes
     */
    private void createComboBoxClasses(){
        try {
            classes = mem.getAllClassesLight(false, false);
            cmbClasses = new ComboBox<>(ts.getTranslatedString("module.datamodelman.inventory-classes"));
            cmbClasses.setWidthFull();
            cmbClasses.setAutofocus(true);
            cmbClasses.setAllowCustomValue(false);
            cmbClasses.setClearButtonVisible(true);
            cmbClasses.setPlaceholder(ts.getTranslatedString("module.configman.filter.label.select-a-classs"));
            cmbClasses.setItems(classes);
            cmbClasses.setRenderer(new ComponentRenderer<>(item -> new IconNameCellGrid(item.toString(), item.getName(), new ClassNameIconGenerator(resourceFactory))));
            cmbClasses.addValueChangeListener(e -> {
                setupFiltersGrid(e.getValue() == null ? null : e.getValue().getName());
            });
            lytClasses.add(cmbClasses);
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    /**
     * Creates a grid for the filters definitions in a class metadata
     */
    private void buildMainGrid(){
        grdFilters = new Grid<>();
        grdFilters.addThemeVariants(GridVariant.LUMO_NO_BORDER, 
                GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
        grdFilters.setSelectionMode(Grid.SelectionMode.SINGLE);
        
        grdFilters.addSelectionListener(e -> {
            e.getFirstSelectedItem().ifPresent(obj ->{
                showFields(true);
                //The editor don't refresh to an empty String its need at least one space
                aceEditorScript.setValue(obj.getScript().isEmpty() ? " " : obj.getScript());
                btnEnableFilter.setChecked(obj.isEnabled());
                btnSaveScriptChanges.getButtonIcon().setColor(obj.getFilter() != null ? "#8CA62D" : "#D90416");
                btnSaveScriptChanges.setToolTip( 
                        obj.getFilter() != null ? 
                                ts.getTranslatedString("module.configman.filter.label.filter-compiled") : 
                                ts.getTranslatedString("module.configman.filter.label.filter-not-compiled"));
                lastFilterSelected = obj;
                lblFilterName.setText(obj.getName());
                btnDeleteFilter.setEnabled(true);
            });
        });
        
        Grid.Column<FilterDefinition> nameColumn = grdFilters.addColumn(TemplateRenderer.<FilterDefinition>of(
        "<div>[[item.name]] &nbsp; <font class=\"text-secondary\">[[item.classToBeApplied]]</font></div>")
        .withProperty("name", FilterDefinition::getName)
        .withProperty("classToBeApplied", FilterDefinition::getClassToBeApplied));
        
        lytFiltersGrid.add(grdFilters);
        createHeaderGrid(nameColumn);
    }
    
    /**
     * Creates the filter in the header of the grid of filters
     */
    private void createHeaderGrid(Grid.Column column){
        //Creates search text input field in the header of the filters grid
        txtSearchFitlerByName = new TextField();
        txtSearchFitlerByName.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));        
        txtSearchFitlerByName.setValueChangeMode(ValueChangeMode.EAGER);
        txtSearchFitlerByName.setWidthFull();
        txtSearchFitlerByName.setSuffixComponent(new ActionIcon(VaadinIcon.SEARCH, ts.getTranslatedString("module.configman.filter.label.search-by-filter-name")));
        filterDefinitionNameToFilter = new FilterForFilterDefinition();
        // object name filter
        txtSearchFitlerByName.addValueChangeListener(event -> {
            filterDefinitionNameToFilter.setFitlerName(event.getValue().isEmpty() ? null : event.getValue());
            showFields(false);
            lastFilterSelected = null;
            grdFilters.getDataProvider().refreshAll();
        });
        // action button layout
        HorizontalLayout lytActionButtons = new HorizontalLayout();
        lytActionButtons.setClassName("left-action-buttons");
        lytActionButtons.setSpacing(false);
        lytActionButtons.setPadding(false);
        lytActionButtons.setMargin(false);
        lytActionButtons.setWidthFull();
        lytActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytActionButtons.add(txtSearchFitlerByName, btnAddNewFilter);
        
        HeaderRow filterRow = grdFilters.appendHeaderRow();
        filterRow.getCell(column).setComponent(lytActionButtons);
    }
       
   /**
     * Creates the crud buttons of the selected filter definition
     */
    private void createFilterDefinitionControlButtons(){    
        btnSaveScriptChanges.addClickListener(e ->{
            try {
                if(lastFilterSelected != null){
                    if(aceEditorScript.getValue().equals(" "))
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.information"),
                                ts.getTranslatedString("module.general.labels.script-is-empty"), 
                                AbstractNotification.NotificationType.INFO, ts).open();
                    else{
                        updateFilterDefinitionAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>(Constants.PROPERTY_ID, lastFilterSelected.getId()),
                            new ModuleActionParameter<>(Constants.PROPERTY_SCRIPT, aceEditorScript.getValue())));

                        grdFilters.getDataProvider().refreshAll();

                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                                ts.getTranslatedString("module.configman.filter.label.filters-script-compilation-successfully"), 
                                AbstractNotification.NotificationType.INFO, ts).open();
                        
                        btnSaveScriptChanges.getButtonIcon().setColor("#8CA62D");
                        btnSaveScriptChanges.setToolTip( ts.getTranslatedString("module.configman.filter.label.filter-compiled"));
                    }
                }
            } catch (ModuleActionException | ScriptCompilationException ex) {
                btnSaveScriptChanges.getButtonIcon().setColor("#D90416");
                btnSaveScriptChanges.setToolTip(  ts.getTranslatedString("module.configman.filter.label.filter-not-compiled"));
                
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), 
                        ts.getTranslatedString("module.configman.filter.label.filters-script-compilation-failed"), 
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        });
        
        btnDeleteFilter.addClickListener(e ->{
            if(lastFilterSelected != null){
                
                this.deleteFilterDefinitionVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter(Constants.PROPERTY_ID, lastFilterSelected.getId()),
                    new ModuleActionParameter(Constants.PROPERTY_CLASSNAME, lastFilterSelected.getClassToBeApplied()),
                    new ModuleActionParameter(Constants.PROPERTY_NAME, lastFilterSelected.getName()))).open();
                
                grdFilters.getDataProvider().refreshAll();
            }
        });
        
        btnEnableFilter.addValueChangeListener(event -> {
            try {
                if(lastFilterSelected != null){
                    updateFilterDefinitionAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>(Constants.PROPERTY_ID, lastFilterSelected.getId()),
                        new ModuleActionParameter<>(Constants.PROPERTY_ENABLED, event.getValue())));

                    grdFilters.getDataProvider().refreshAll();

                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success")
                            , (event.getValue() ? 
                                    ts.getTranslatedString("module.configman.filters.notification-enable") 
                                    : ts.getTranslatedString("module.configman.filters.notification-diseble"))
                            , AbstractNotification.NotificationType.INFO, ts).open();
                }
            } catch (ModuleActionException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        });
        
        btnEditFilterDefinition.addClickListener(e -> {
            if(lastFilterSelected != null){
                updateFilterDefinitionVisualAction.getVisualComponent(new ModuleActionParameterSet(
                  new ModuleActionParameter<>(Constants.PROPERTY_ID, lastFilterSelected.getId()),
                  new ModuleActionParameter<>(Constants.PROPERTY_NAME, lastFilterSelected.getName()),
                  new ModuleActionParameter<>(Constants.PROPERTY_DESCRIPTION, lastFilterSelected.getDescription()))).open();
            }
        });
    
    } 

    /**
     * When a class is selected form the grid classes, but before the selection 
     * of a filter 
     * @param className the class name selected
     */
    private void setupFiltersGrid(String className) {
        showFields(false);
        lastFilterSelected = null;
        if(className == null){
            showFields(false);
            btnDeleteFilter.setEnabled(false);
            createFiltersDataProviderGrid(className);
        }
        else{
            for (ClassMetadataLight c : classes) {
                if(c.getName().equals(className)){
                    cmbClasses.setValue(c);
                    break;
                }
            }
            createFiltersDataProviderGrid(className);
        }
    }
    
    /**
     *  Shows/Hides the labels and buttons in the header of the filter editor
     */
    private void showFields(boolean show){
        btnEditFilterDefinition.setVisible(show);
        aceEditorScript.setVisible(show);
        lblFilter.setVisible(show);
        lytScriptControls.setVisible(show);
        if(!show){
            aceEditorScript.setValue(" ");
            aceEditorScript.setVisible(false);
        }
    }
    
    /**
     * Creates a provider for the grid filters
     * @param className the class name to retrieve its filters, null to retrieve all the filters
     */
    public void createFiltersDataProviderGrid(String className){
        provider = DataProvider.fromFilteringCallbacks(query ->{

            FilterForFilterDefinition filteredName = query.getFilter().orElse(null);
            HashMap<String, Object> valuesToFilter = new HashMap<>();
            valuesToFilter.put(Constants.PROPERTY_NAME, filteredName.getFitlerName());
        
            try {
                List<FilterDefinition> fs;
                if(className == null)
                    fs = aem.getAllFilterDefinitions(valuesToFilter, query.getOffset(), query.getLimit());
                else    
                    fs = aem.getFilterDefinitionsForClass(className, false, true, valuesToFilter, query.getOffset(), query.getLimit());
                
                if(fs.size() == 1){
                    grdFilters.select(fs.get(0));
                    btnDeleteFilter.setEnabled(true);
                    showFields(true);
                    lblFilterName.setText(fs.get(0).getName());
                }
                
                return fs.stream();
            } catch (InvalidArgumentException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
            return Collections.EMPTY_LIST.stream();
            
        }, query -> {
            FilterForFilterDefinition filteredName = query.getFilter().orElse(null);
            HashMap<String, Object> valuesToFilter = new HashMap<>();
            valuesToFilter.put(Constants.PROPERTY_NAME, filteredName.getFitlerName());
        
            
             try {
                if(className == null)
                    return (int)aem.getAllFilterDefinitionsCount(valuesToFilter);
                else
                    return (int)aem.getFilterDefinitionsForClassCount(className, false, true, valuesToFilter, query.getOffset(), query.getLimit());
            } catch (InvalidArgumentException ex) {
                 new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                         AbstractNotification.NotificationType.ERROR, ts).open();
            }
            return 0;
        });
        
        dpConfigurableFilter = provider.withConfigurableFilter();
        dpConfigurableFilter.setFilter(filterDefinitionNameToFilter);
        
        grdFilters.setDataProvider(dpConfigurableFilter);
        grdFilters.getDataProvider().refreshAll();
    }
    
    @Override
    public void initContent() {
        setSizeFull();
        setMargin(false);
        setSpacing(false);
        setPadding(false);
        //control buttons
        this.newFilterDefinitionVisualAction.registerActionCompletedLister(this);
        this.updateFilterDefinitionVisualAction.registerActionCompletedLister(this);
        this.deleteFilterDefinitionVisualAction.registerActionCompletedLister(this);
        
        lblFilterName = new Label(ts.getTranslatedString("module.configman.filter.label.select-a-filter"));
        lblFilterName.setClassName("dialog-title");
                       
        btnEnableFilter = new PaperToggleButton();
        btnEnableFilter.getElement().setProperty("title", ts.getTranslatedString("module.general.labels.enable"));
        btnEnableFilter.setClassName("green", true);
        
        btnAddNewFilter = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O), this.newFilterDefinitionVisualAction.getModuleAction().getDescription());
        btnAddNewFilter.addClickListener( (e) -> {
                    this.newFilterDefinitionVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(NewFilterDefinitionVisualAction.ALL_PARAM_CLASSES, classes),
                        new ModuleActionParameter(Constants.PROPERTY_CLASSNAME, cmbClasses.getValue() == null ? null : cmbClasses.getValue().getName())
                    )).open();
                });
        btnAddNewFilter.setHeight("32px");
        
        btnDeleteFilter= new ActionButton(new ActionIcon(VaadinIcon.TRASH), deleteFilterDefinitionVisualAction.getModuleAction().getDescription());
        btnDeleteFilter.setEnabled(false);
        
        btnSaveScriptChanges = new ActionButton( new ActionIcon(VaadinIcon.HAMMER), ts.getTranslatedString("module.configman.filters.actions.update-filter.label.save-script-build-filter"));
        
        btnEditFilterDefinition = new ActionButton(new ActionIcon(VaadinIcon.EDIT), updateFilterDefinitionVisualAction.getModuleAction().getDescription());
        btnEditFilterDefinition.setVisible(false);
        
        lblFilter = new Label(String.format("%s: ", ts.getTranslatedString("module.general.labels.filter")));
        lblFilter.setClassName("dialog-title");
        lblFilter.setVisible(false);        
        //end control buttons
        
        splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(25);
        //--left side
        // Main left Layout 
        VerticalLayout lytLeftSide = new VerticalLayout();
        lytLeftSide.setClassName("left-side");
        lytLeftSide.setSpacing(false);
        lytLeftSide.setPadding(false);
        lytLeftSide.setMargin(false);
        lytLeftSide.setHeightFull();
        lytLeftSide.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);      
        //top grid
        lytClasses = new VerticalLayout();
        lytClasses.setClassName("top-grid");
        lytClasses.setSpacing(true);
        lytClasses.setPadding(false);
        lytClasses.setMargin(false);
        createComboBoxClasses();
        //bottom grid
        lytFiltersGrid = new VerticalLayout();
        lytFiltersGrid.setClassName("bottom-grid");
        lytFiltersGrid.setSpacing(false);
        lytFiltersGrid.setPadding(false);
        lytFiltersGrid.setMargin(false);
        lytFiltersGrid.setHeightFull();
        buildMainGrid();
              
        lytLeftSide.add(lytClasses, lytFiltersGrid);        
        //end left side
        splitLayout.addToPrimary(lytLeftSide);
    
        //--Right side            
        VerticalLayout lytRightMain = new VerticalLayout();
        lytRightMain.setClassName("right-side");        
        lytRightMain.setMargin(false);
        lytRightMain.setPadding(false);
        lytRightMain.setSpacing(false);
        
        HorizontalLayout lytFilterName = new HorizontalLayout();
        lytFilterName.setPadding(false);
        lytFilterName.setMargin(false);
        lytFilterName.setSpacing(true);
        lytFilterName.setWidth("35%");
        lytFilterName.add(new Html("<span>&nbsp;</span>"), lblFilter, lblFilterName);
        // Layout for action buttons
        HorizontalLayout lytRightActionButtons = new HorizontalLayout();
        lytRightActionButtons.setClassName("button-toolbar");
        lytRightActionButtons.setPadding(false);
        lytRightActionButtons.setMargin(false);
        lytRightActionButtons.setSpacing(false);        
        lytRightActionButtons.setJustifyContentMode(JustifyContentMode.END);
        lytRightActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytRightActionButtons.add(btnEditFilterDefinition, btnDeleteFilter, btnSaveScriptChanges, btnEnableFilter);
                
        lytScriptControls = new HorizontalLayout();
        lytScriptControls.setClassName("script-control");
        lytScriptControls.setPadding(false);
        lytScriptControls.setMargin(false);
        lytScriptControls.setSpacing(false);
        lytScriptControls.setWidthFull();
        lytScriptControls.setVisible(false);
        lytScriptControls.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytScriptControls.add(lytFilterName, lytRightActionButtons);
                
        VerticalLayout lytScriptHeader = new VerticalLayout(lytScriptControls);
        lytScriptHeader.setClassName("header-script-control");
        lytScriptHeader.setWidth(DEFAULT_WIDTH);
        lytScriptHeader.setPadding(false);
        lytScriptHeader.setMargin(false);
        lytScriptHeader.setSpacing(false);
        lytScriptHeader.setSpacing(false);
        
        //editor
        aceEditorScript = new AceEditor();
        aceEditorScript.setMode(AceMode.groovy);
        aceEditorScript.setFontsize(12);
        aceEditorScript.setVisible(false);
        
        lytScriptEditor = new VerticalLayout();
        lytScriptEditor.setClassName("script-editor");
        lytScriptEditor.setMargin(false);
        lytScriptEditor.setSpacing(false);
        lytScriptEditor.setPadding(false);
        lytScriptEditor.add(aceEditorScript);

        lytRightMain.add(lytScriptHeader, lytScriptEditor);       
        //end right sideFilterForValidatorDefinition
        splitLayout.addToSecondary(lytRightMain);
        
        add(splitLayout);
        
        
        createFilterDefinitionControlButtons();
        setupFiltersGrid(null);
    }
    
    /**
     * Used to wrap the values to Filter in the grid of filter definitions
     */
    public class FilterForFilterDefinition {
        private String fitlerName;
        private String filterEnable;

        public String getFitlerName() {
            return fitlerName;
        }

        public void setFitlerName(String fitlerName) {
            this.fitlerName = fitlerName;
        }

        public String getFilterEnable() {
            return filterEnable;
        }

        public void setFilterEnable(String filterEnable) {
            this.filterEnable = filterEnable;
        }
    }
}
