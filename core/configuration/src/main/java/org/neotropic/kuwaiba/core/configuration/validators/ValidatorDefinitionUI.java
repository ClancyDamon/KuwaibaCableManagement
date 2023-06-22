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
package org.neotropic.kuwaiba.core.configuration.validators;

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
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ValidatorDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.configuration.ConfigurationManagerLayout;
import org.neotropic.kuwaiba.core.configuration.validators.actions.DeleteValidatorDefinitionVisualAction;
import org.neotropic.kuwaiba.core.configuration.validators.actions.NewValidatorDefinitionVisualAction;
import org.neotropic.kuwaiba.core.configuration.validators.actions.UpdateValidatorDefinitionAction;
import org.neotropic.kuwaiba.core.configuration.validators.actions.UpdateValidatorDefinitionVisualAction;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the Validator Definition module. This class manages how the pages
 * corresponding to different functionalities are presented in a single place.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route(value = "configuration/validators", layout = ConfigurationManagerLayout.class)
public class ValidatorDefinitionUI extends VerticalLayout implements ActionCompletedListener,
        HasDynamicTitle, AbstractUI {
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
     * The visual action to create a validator definition
     */
    @Autowired
    private NewValidatorDefinitionVisualAction newValidatorDefinitionVisualAction;
    /**
     * The visual action to delete a validator definition
     */
    @Autowired
    private DeleteValidatorDefinitionVisualAction deleteValidatorDefinitionVisualAction;
    /**
     * Visual action to update the validator
     */
    @Autowired
    private UpdateValidatorDefinitionVisualAction updateValidatorDefinitionVisualAction;
    /**
     * action to update the validator
     */
    @Autowired
    private UpdateValidatorDefinitionAction updateValidatorDefinitionAction;
    /**
     * Button used to delete a validator definition
     */
    private ActionButton btnDeleteValidator;
    /**
     * Button used to save changes of a validator definition
     */
    private ActionButton btnSavesScriptChanges;
    /**
     * Button used to update validator definition properties
     */
    private ActionButton btnEditValidator;
    /**
     * Button used to create new Validator
     */
    private ActionButton btnAddNewValidator;
    /**
     * Enables/disables the vaslidator
     */
    private PaperToggleButton btnEnableValidator;
    /**
     * The grid with the validators
     */
    private Grid<ValidatorDefinition> gridValidators;
    /**
     * validator name
     */
    private Label lblValidatorName;
    /**
     * Current selected validator
     */
    private ValidatorDefinition currentValidator;
    /**
     * Current selected filter
     */
    private String currentFilter;
    /**
     * Contains the script editor in the right side, bottom
     */
    private VerticalLayout lytScriptEditor;
    /**
     * Component to edit the script
     */
    private AceEditor aceEditorScript;
    /**
     * Contains validator selected name
     */
    private HorizontalLayout lytFilterName;
    /**
     * Contains the script editor header
     */
    private VerticalLayout lytScriptHeader;
    /**
     * Current selected class
     */
    private ClassMetadataLight currentClass;
    /**
     * Split the content
     */
    private SplitLayout splitLayout;
    /**
     * Object to add a new Class
     */
    private ClassMetadataLight allValidators;
    /**
     * Combo filter for classes
     */
    private ComboBox<ClassMetadataLight> cmbValidatorClass;
    /**
     * Layout of validator definition
     */
    private VerticalLayout lytValidatorsGrid;
    /**
     * Layout for actions of validator definition
     */
    private HorizontalLayout lytRightActionButtons;
    /**
     * Layout for actions buttons over script editor
     */
    private HorizontalLayout lytScriptControls;
    /**
     * Main layout
     */
    private VerticalLayout lytLeftSide;
    /**
     * Object to show info
     */
    private Label lblInfo;
    /**
     * The grid provider
     */
    private ListDataProvider<ValidatorDefinition> dataProvider;

    public ValidatorDefinitionUI() {
        super();
        setSizeFull();
    }

    @Override
    public void onDetach(DetachEvent ev) {
        this.newValidatorDefinitionVisualAction.unregisterListener(this);
        this.deleteValidatorDefinitionVisualAction.unregisterListener(this);
        this.updateValidatorDefinitionVisualAction.unregisterListener(this);
    }

    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent event) {
        if (event.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), event.getMessage(),
                    AbstractNotification.NotificationType.INFO, ts).open();
            refreshValidatorsGrid();
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), event.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
    }

    private void createClasses() {
        try {
            // List of classes for filter
            List<ClassMetadataLight> classes = mem.getSubClassesLight(Constants.CLASS_INVENTORYOBJECT, true, true);
            allValidators = new ClassMetadataLight(0, ts.getTranslatedString("module.configman.validators.label.all-validators"),
                    ts.getTranslatedString("module.configman.validators.label.all-validators"));
            classes.add(allValidators);
            // First filter
            cmbValidatorClass = new ComboBox<>(ts.getTranslatedString("module.datamodelman.inventory-classes"));
            cmbValidatorClass.setPlaceholder(ts.getTranslatedString("module.configman.validators.label.choose-class"));
            cmbValidatorClass.setWidthFull();
            cmbValidatorClass.setItems(classes);
            cmbValidatorClass.setAllowCustomValue(false);
            cmbValidatorClass.setClearButtonVisible(true);
            cmbValidatorClass.setValue(allValidators);
            cmbValidatorClass.setItemLabelGenerator(ClassMetadataLight::getName);
            cmbValidatorClass.addValueChangeListener(event -> {
                showFields(false);
                currentClass = event.getValue();
                currentFilter = event.getValue() != null ? event.getValue().getName() : null;
                buildMainGrid();                
            });
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    /**
     * Creates a provider for the grid filters
     *
     */
    public void createDataProviderGrid() {
        if (currentFilter != null) {
            List<ValidatorDefinition> validators = aem.getValidatorDefinitionsForClass(currentFilter);
            dataProvider = new ListDataProvider<>(validators);
        } else {
            List<ValidatorDefinition> validators = aem.getAllValidatorDefinitions();
            dataProvider = new ListDataProvider<>(validators);
        }
        gridValidators.setDataProvider(dataProvider);
        refreshValidatorsGrid();
    }

    /**
     * Build grid with validator definitions a specific class from
     *
     * @param classMetadataLight The class to retrieve the validator definitions
     * from
     */
    private void buildMainGrid() {
        gridValidators = new Grid<>();
        gridValidators.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
        gridValidators.setSelectionMode(Grid.SelectionMode.SINGLE);

        gridValidators.addSelectionListener(event -> {
            event.getFirstSelectedItem().ifPresent(obj -> {
                showFields(true);
                //The editor don't refresh to an empty String its need at least one space
                aceEditorScript.setValue(obj.getScript().isEmpty() ? " " : obj.getScript());
                btnEnableValidator.setChecked(obj.isEnabled());
                currentValidator = obj;
                lblValidatorName.setText(obj.getName());
            });
        });

        Grid.Column<ValidatorDefinition> nameColumn = gridValidators
                .addColumn(TemplateRenderer.<ValidatorDefinition>of(
                        "<div>[[item.name]] &nbsp; <font class=\"text-secondary\">[[item.classToBeApplied]]</font></div>")
                        .withProperty("name", ValidatorDefinition::getName)
                        .withProperty("classToBeApplied", ValidatorDefinition::getClassToBeApplied));

        lytValidatorsGrid.removeAll();
        lytValidatorsGrid.add(gridValidators);
        createHeaderGrid(nameColumn);
        createDataProviderGrid();

    }

    /**
     * Creates the filter in the header of the grid of filters
     */
    private void createHeaderGrid(Grid.Column column) {
        //Creates search text input field in the header of the filters grid        
        TextField txtSearchFilterByName = new TextField();
        txtSearchFilterByName.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtSearchFilterByName.setValueChangeMode(ValueChangeMode.EAGER);
        txtSearchFilterByName.setWidthFull();
        txtSearchFilterByName.setSuffixComponent(new ActionIcon(VaadinIcon.SEARCH, ts.getTranslatedString("module.configman.validators.label.filter-validator")));
        // object name filter
        txtSearchFilterByName.addValueChangeListener(event -> {
            dataProvider.addFilter(validator -> StringUtils.containsIgnoreCase(validator.getName(),
                    txtSearchFilterByName.getValue()));
            showFields(false);
            currentValidator = null;
            refreshValidatorsGrid();
        });

        // action button layout
        HorizontalLayout lytActionButtons = new HorizontalLayout();
        lytActionButtons.setClassName("left-action-buttons");
        lytActionButtons.setSpacing(false);
        lytActionButtons.setPadding(false);
        lytActionButtons.setMargin(false);
        lytActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytActionButtons.add(txtSearchFilterByName, btnAddNewValidator);

        HeaderRow filterRow = gridValidators.appendHeaderRow();
        filterRow.getCell(column).setComponent(lytActionButtons);
    }

    private void refreshValidatorsGrid() {
        gridValidators.getDataProvider().refreshAll();
    }

    @Override
    public void initContent() {
        setSizeFull();
        setMargin(false);
        setSpacing(false);
        setPadding(false);
        //control buttons 
        this.newValidatorDefinitionVisualAction.registerActionCompletedLister(this);
        this.deleteValidatorDefinitionVisualAction.registerActionCompletedLister(this);
        this.updateValidatorDefinitionVisualAction.registerActionCompletedLister(this);

        Command saveAfterEvent = () -> createDataProviderGrid();
        btnAddNewValidator = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O), this.newValidatorDefinitionVisualAction.getModuleAction().getDisplayName());
        btnAddNewValidator.addClickListener(event -> {
            this.newValidatorDefinitionVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("class", currentClass),
                    new ModuleActionParameter("commandClose", saveAfterEvent)
            )).open();
        });
        btnAddNewValidator.setHeight("32px");

        //end control buttons
        // Split Layout
        splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(25);
        //--left side        
        // Main left Layout 
        lytLeftSide = new VerticalLayout();
        lytLeftSide.setClassName("left-side");
        lytLeftSide.setMargin(false);
        lytLeftSide.setSpacing(false);
        lytLeftSide.setPadding(false);
        lytLeftSide.setId("main-lyt");
        lytLeftSide.setHeightFull();
        lytLeftSide.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
        //top grid
        VerticalLayout lytClasses = new VerticalLayout();
        lytClasses.setClassName("top-grid");
        lytClasses.setSpacing(true);
        lytClasses.setMargin(false);
        lytClasses.setPadding(false);
        createClasses();
        lblInfo = new Label();
        lblInfo.setVisible(true);
        lblInfo.setId("info-id");
        lytClasses.add(cmbValidatorClass, lblInfo);
        // bottom grid                  
        lytValidatorsGrid = new VerticalLayout();
        lytValidatorsGrid.setClassName("bottom-grid");
        lytValidatorsGrid.setSpacing(false);
        lytValidatorsGrid.setPadding(false);
        lytValidatorsGrid.setMargin(false);
        lytValidatorsGrid.setHeightFull();
        buildMainGrid();
        lytValidatorsGrid.add(gridValidators);

        lytLeftSide.add(lytClasses, lytValidatorsGrid);
        //end left side
        splitLayout.addToPrimary(lytLeftSide);

        //--Right side     
        VerticalLayout lytRightMain = new VerticalLayout();
        lytRightMain.setClassName("right-side");
        lytRightMain.setMargin(false);
        lytRightMain.setPadding(false);
        lytRightMain.setSpacing(false);

        // Layout for action buttons
        lytRightActionButtons = new HorizontalLayout();
        lytRightActionButtons.setClassName("button-toolbar");
        lytRightActionButtons.setPadding(false);
        lytRightActionButtons.setMargin(false);
        lytRightActionButtons.setSpacing(false);
        lytRightActionButtons.setJustifyContentMode(JustifyContentMode.END);
        lytRightActionButtons.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        lytFilterName = new HorizontalLayout();
        lytFilterName.setMargin(false);
        lytFilterName.setPadding(false);
        lytFilterName.setWidth("35%");

        createRightControlButtons();
        lytScriptControls = new HorizontalLayout();
        lytScriptControls.setClassName("script-control");
        lytScriptControls.setPadding(false);
        lytScriptControls.setMargin(false);
        lytScriptControls.setSpacing(false);
        lytScriptControls.setWidthFull();
        lytScriptControls.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytScriptControls.add(lytFilterName, lytRightActionButtons);

        lytScriptHeader = new VerticalLayout();
        lytScriptHeader.setClassName("header-script-control");
        lytScriptHeader.setPadding(false);
        lytScriptHeader.setMargin(false);
        lytScriptHeader.setSpacing(false);
        lytScriptHeader.setSpacing(false);
        lytScriptHeader.add(lytScriptControls);
        lytScriptHeader.setVisible(false);

        //editor
        aceEditorScript = new AceEditor();
        aceEditorScript.setMode(AceMode.groovy);
        aceEditorScript.setFontsize(12);

        lytScriptEditor = new VerticalLayout();
        lytScriptEditor.setClassName("script-editor");
        lytScriptEditor.setMargin(false);
        lytScriptEditor.setSpacing(false);
        lytScriptEditor.setPadding(false);
        lytScriptEditor.add(aceEditorScript);
        lytScriptEditor.setVisible(false);

        lytRightMain.add(lytScriptHeader, lytScriptEditor);
        //end right side
        splitLayout.addToSecondary(lytRightMain);

        add(splitLayout);
    }

    /**
     * define control buttons and behavior
     */
    void createRightControlButtons() {

        Label lblValidator = new Label(ts.getTranslatedString("module.configman.validators.header-name"));
        lblValidator.setClassName("dialog-title");
        lblValidatorName = new Label();
        lblValidatorName.setClassName("dialog-title");
        lytFilterName.add(new Html("<span>&nbsp;</span>"), lblValidator, lblValidatorName);

        Command deleteAfterEvent = () -> {
            showFields(false);
            createDataProviderGrid();
        };
        
        Command editAfterEvent = () -> {
            showFields(false);
            createDataProviderGrid();
        };

        btnDeleteValidator = new ActionButton(new ActionIcon(VaadinIcon.TRASH), this.deleteValidatorDefinitionVisualAction.getModuleAction().getDisplayName());
        btnDeleteValidator.addClickListener(event -> {
                    this.deleteValidatorDefinitionVisualAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("validatorDefinition", currentValidator),
                            new ModuleActionParameter("commandClose", deleteAfterEvent)
                    )).open();
                });

        btnEditValidator = new ActionButton(new ActionIcon(VaadinIcon.EDIT), this.updateValidatorDefinitionVisualAction.getModuleAction().getDisplayName());
        btnEditValidator.addClickListener(event -> {
            updateValidatorDefinitionVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter<>(Constants.PROPERTY_ID, currentValidator.getId()),
                    new ModuleActionParameter<>(Constants.PROPERTY_NAME, currentValidator.getName()),
                    new ModuleActionParameter<>(Constants.PROPERTY_DESCRIPTION, currentValidator.getDescription()),
                    new ModuleActionParameter("commandClose", editAfterEvent)
            )).open();
        });

        btnSavesScriptChanges = new ActionButton(new ActionIcon(VaadinIcon.DOWNLOAD), ts.getTranslatedString("module.configman.validators.properties-general.button-save"));
        btnSavesScriptChanges.addClickListener(event -> {
            try {
                updateValidatorDefinitionAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>(Constants.PROPERTY_ID, currentValidator.getId()),
                        new ModuleActionParameter<>(Constants.PROPERTY_SCRIPT, aceEditorScript.getValue())
                ));
                createDataProviderGrid();
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                        ts.getTranslatedString("module.configman.validators.properties-script.notification-saved"),
                        AbstractNotification.NotificationType.INFO, ts).open();
            } catch (ModuleActionException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        });

        btnEnableValidator = new PaperToggleButton();
        btnEnableValidator.getElement().setProperty("title", ts.getTranslatedString("module.general.labels.enable"));
        btnEnableValidator.setClassName("green", true);
        btnEnableValidator.addValueChangeListener(event -> {
            try {
                updateValidatorDefinitionAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>(Constants.PROPERTY_ID, currentValidator.getId()),
                        new ModuleActionParameter<>(Constants.PROPERTY_ENABLED, event.getValue())
                ));
                createDataProviderGrid();
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                        (event.getValue()
                        ? ts.getTranslatedString("module.general.labels.enabled")
                        : ts.getTranslatedString("module.general.labels.disabled")),
                        AbstractNotification.NotificationType.INFO, ts).open();
            } catch (ModuleActionException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        });
        lytRightActionButtons.add(btnDeleteValidator, btnEditValidator, btnSavesScriptChanges, btnEnableValidator);
    }

    /**
     * Shows/Hides the labels and buttons in the header of the filter editor
     */
    private void showFields(boolean show) {
        lytScriptHeader.setVisible(show);
        lytScriptEditor.setVisible(show);
        if (!show) {
            aceEditorScript.setValue(" ");
        }
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.configman.validators.title");
    }
}
