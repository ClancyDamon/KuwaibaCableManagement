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
package org.neotropic.kuwaiba.modules.commercial.sync.actions;

import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.UnsupportedPropertyException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.sync.SynchronizationModule;
import org.neotropic.kuwaiba.modules.commercial.sync.SynchronizationService;
import org.neotropic.kuwaiba.modules.commercial.sync.components.DataSourceComponent;
import org.neotropic.kuwaiba.modules.commercial.sync.components.ParameterItemDataSource;
import org.neotropic.kuwaiba.modules.commercial.sync.components.ValidationMessage;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncDataSourceCommonParameters;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncDataSourceConfiguration;
import org.neotropic.kuwaiba.modules.commercial.sync.model.TemplateDataSource;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Visual wrapper of configure data source.
 *
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewSyncDataSourceConfigurationVisualAction extends AbstractVisualAdvancedAction {

    /**
     * New business object visual action parameter business object.
     */
    public static String PARAM_BUSINESS_OBJECT = "businessObject"; //NOI18N
    /**
     * Parameter group.
     */
    public static String PARAM_GROUP = "group";
    /**
     * Parameter exception.
     */
    public static String PARAM_EXCEPTION = "exception";
    /**
     * Parameter command close.
     */
    public static String PARAM_COMMANDCLOSE = "commandClose";
    private Binder<ParameterItemDataSource> binderCommonParemers;
    private Binder<ParameterItemDataSource> binderSpecificParemers;
    private BindingValidationStatus<?> verifiedHandler;
    /**
     * New Sync Data Source Configuration
     */
    private SyncDataSourceConfiguration syncDataSourceConfiguration;
    /**
     * The combo box with common parameters list
     */
    private ComboBox<TemplateDataSource> cmbDataSourceType;
    /**
     * The grid with sync data source configurations list
     */
    private Grid<SyncDataSourceConfiguration> gridSourceConfigurations;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Synchronization Service
     */
    @Autowired
    private SynchronizationService ss;
    /**
     * Reference of the module Action to configure sync data source.
     */
    @Autowired
    private NewSyncDataSourceConfigurationAction newSyncDataSourceConfigurationAction;
    /**
     * Close action command
     */
    private Command commandClose;
    /**
     * Window to configure sync data source
     */
    private ConfirmDialog wdwConfigureDataSource;
    /**
     * Object to validate
     */
    private Boolean success = true;

    public NewSyncDataSourceConfigurationVisualAction() {
        super(SynchronizationModule.MODULE_ID);
    }

    private static void addCloseHandler(com.vaadin.flow.component.Component textField, Editor<ParameterItemDataSource> editor) {
        textField.getElement().addEventListener("keydown", e -> editor.cancel())
                .setFilter("event.code === 'Escape'");
    }

    @Override
    public String appliesTo() {
        return Constants.CLASS_GENERICCOMMUNICATIONSELEMENT;
    }

    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        BusinessObjectLight selectedObject;
        if (parameters.containsKey(PARAM_BUSINESS_OBJECT)) {

            selectedObject = (BusinessObjectLight) parameters.get(PARAM_BUSINESS_OBJECT);
            commandClose = (Command) parameters.get(PARAM_COMMANDCLOSE);

            wdwConfigureDataSource = new ConfirmDialog(ts
                    , ts.getTranslatedString("module.sync.actions.get-sync-group.associated-data-source")
                    , ts.getTranslatedString("module.sync.data-source.button.accept")
            );
            wdwConfigureDataSource.getBtnConfirm().setEnabled(false);
            wdwConfigureDataSource.setResizable(true);
            wdwConfigureDataSource.setDraggable(true);
            createDialogContent(wdwConfigureDataSource, selectedObject);
            // Configurate
            wdwConfigureDataSource.getBtnConfirm().addClickListener(event -> {
                saveDatasource(syncDataSourceConfiguration);
            });

            return wdwConfigureDataSource;
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.general.messages.object-not-found")));
    }

    private void createDialogContent(ConfirmDialog wdwConfigureDataSource, BusinessObjectLight selectedObject) {

        Div labelDataSource = new Div();
        HorizontalLayout lytDataSourceHeader = new HorizontalLayout();
        TextField txtDataSourceType = new TextField(ts.getTranslatedString("module.sync.new.data-source.type.label"));
        TextField txtDataSourceName = new TextField(ts.getTranslatedString("module.sync.new.data-source.name.label"));
        TextArea txaDataSourceDescription = new TextArea(ts.getTranslatedString("module.sync.data-source.grid.description"));
        cmbDataSourceType = new ComboBox<>(ts.getTranslatedString("module.sync.template-data-source.title"));
        Grid<ParameterItemDataSource> grdDataSourceCommons = new Grid<>();
        Grid<ParameterItemDataSource> grdDataSourceSpecific = new Grid<>();
        syncDataSourceConfiguration = new SyncDataSourceConfiguration();
        Div divCurrentDatasources = new Div();

        try {
            divCurrentDatasources.getStyle().set("box-sizing", "border-box");
            divCurrentDatasources.getStyle().set("border", "1px solid #e5e5e5");
            divCurrentDatasources.getStyle().set("border-radius", "var(--lumo-border-radius, 0.1em)");
            divCurrentDatasources.getStyle().set("min-height", "64px");
            divCurrentDatasources.getStyle().set("width", "auto");
            divCurrentDatasources.getStyle().set("min-width", "30%");
            divCurrentDatasources.getStyle().set("max-width", "100%");
            divCurrentDatasources.getStyle().set("display", "flex");
            divCurrentDatasources.getStyle().set("flex-wrap", "wrap");
            divCurrentDatasources.getStyle().set("align-items", "center");
            List<SyncDataSourceConfiguration> syncDataSrcList = ss.getSyncDataSrcByBussinesObject(selectedObject.getId(), null, -1, -1);
            syncDataSrcList.stream().map(item -> {
                DataSourceComponent dataSourceComponent = new DataSourceComponent(item);
                dataSourceComponent.getStyle().set("box-sizing", "border-box");
                dataSourceComponent.getStyle().set("border", "1px solid #e5e5e5");
                dataSourceComponent.getStyle().set("border-radius", "var(--lumo-border-radius, 0.1em)");
                dataSourceComponent.getStyle().set("width", "auto");
                dataSourceComponent.getStyle().set("min-width", "21%");
                dataSourceComponent.getStyle().set("max-width", "100%");
                dataSourceComponent.getStyle().set("display", "flex");
                dataSourceComponent.getStyle().set("flex-wrap", "wrap");
                dataSourceComponent.getStyle().set("align-items", "center");
                dataSourceComponent.getStyle().set("text-align", "center");
                dataSourceComponent.getStyle().set("padding", "2px 2px 2px 2px");
                dataSourceComponent.getStyle().set("background-color", "green");
                dataSourceComponent.getStyle().set("color", "white");
                dataSourceComponent.getStyle().set("font-size", "0.775em");
                return dataSourceComponent;
            }).forEach(item -> divCurrentDatasources.add(item));

            wdwConfigureDataSource.getBtnConfirm().setEnabled(false);
            wdwConfigureDataSource.getBtnConfirm().removeClassName("confirm-button");
            createCbmCommonPropertiesProvider();
            //binder general properties
            syncDataSourceConfiguration.setBusinessObjectLight(selectedObject);
            if (syncDataSourceConfiguration.getTemplateDataSource() != null)
                cmbDataSourceType.setValue(syncDataSourceConfiguration.getTemplateDataSource());

            if (syncDataSourceConfiguration.getName() != null)
                txtDataSourceName.setValue(syncDataSourceConfiguration.getName());

            if (syncDataSourceConfiguration.getCommonParameters() != null
                    && syncDataSourceConfiguration.getCommonParameters().getDataSourcetype() != null)
                txtDataSourceType.setValue(syncDataSourceConfiguration.getCommonParameters().getDataSourcetype());

            if (syncDataSourceConfiguration.getDescription() != null)
                txaDataSourceDescription.setValue(syncDataSourceConfiguration.getDescription());

            cmbDataSourceType.addValueChangeListener(event -> {
                if (event.getValue() != null) {
                    syncDataSourceConfiguration.setTemplateDataSource(event.getValue());
                    SyncDataSourceCommonParameters commonParameters = new SyncDataSourceCommonParameters(event.getValue().getName());
                    commonParameters.setParameters(event.getValue().getParameters());
                    syncDataSourceConfiguration.setCommonParameters(commonParameters);
                    txtDataSourceType.setValue(event.getValue().getName());
                    txtDataSourceName.setValue(String.format("%s-", event.getValue().getName()));
                    grdDataSourceCommons.setItems(syncDataSourceConfiguration.getCommonParameters().getParameterToItem());
                } else {
                    txtDataSourceName.clear();
                    txtDataSourceType.clear();
                    grdDataSourceCommons.setItems(new ArrayList<>());
                }
                validateSave(syncDataSourceConfiguration);
            });

            grdDataSourceCommons.setHeight("10em");
            grdDataSourceSpecific.setHeight("10em");
            cmbDataSourceType.setWidthFull();
            cmbDataSourceType.setRequired(true);
            cmbDataSourceType.setRequiredIndicatorVisible(true);
            cmbDataSourceType.setAllowCustomValue(true);
            cmbDataSourceType.setItemLabelGenerator(item -> item.getName());
            cmbDataSourceType.setRenderer(getRenderer());
            txtDataSourceType.setReadOnly(true);
            txtDataSourceName.setRequired(true);
            txtDataSourceName.setRequiredIndicatorVisible(true);
            txtDataSourceName.setValueChangeMode(ValueChangeMode.EAGER);
            txtDataSourceName.addValueChangeListener(event -> {
                syncDataSourceConfiguration.setName(event.getValue());
                validateSave(syncDataSourceConfiguration);
            });
            txaDataSourceDescription.setValueChangeMode(ValueChangeMode.EAGER);
            txaDataSourceDescription.setWidthFull();
            txaDataSourceDescription.addValueChangeListener(event -> {
                syncDataSourceConfiguration.setDescription(event.getValue());
                validateSave(syncDataSourceConfiguration);
            });

            labelDataSource.getElement().setProperty("innerHTML", ts.getTranslatedString("module.sync.new.data-source.label"));
            //create right layout
            lytDataSourceHeader.addAndExpand(txtDataSourceType);
            lytDataSourceHeader.addAndExpand(txtDataSourceName);

            wdwConfigureDataSource.add(divCurrentDatasources, labelDataSource, cmbDataSourceType,
                    lytDataSourceHeader, txaDataSourceDescription);

            createCommonsGrid(grdDataSourceCommons, syncDataSourceConfiguration);
            createSpecificGrid(grdDataSourceSpecific, syncDataSourceConfiguration);

            validateSave(syncDataSourceConfiguration);
        } catch (InvalidArgumentException | ApplicationObjectNotFoundException | UnsupportedPropertyException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.information"),
                    ex.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    /**
     * data source common properties provider
     */
    private void createCbmCommonPropertiesProvider() {
        DataProvider<TemplateDataSource, String> commonPropertiesDataProvider = DataProvider.fromFilteringCallbacks(
                query -> {
                    List<TemplateDataSource> elements;
                    //filters
                    Optional<String> objectName = query.getFilter();
                    HashMap<String, String> filters = null;
                    try {
                        if (objectName.isPresent() && !objectName.get().trim().isEmpty()) {
                            filters = new HashMap<>();
                            filters.put(Constants.PROPERTY_NAME, objectName.get());
                        }
                        elements = ss.getTemplateDataSrc(filters, query.getOffset(), query.getLimit());
                    } catch (ApplicationObjectNotFoundException | InvalidArgumentException |
                             UnsupportedPropertyException ex) {
                        elements = new ArrayList<>();
                    }
                    return elements.stream();
                },
                query -> {
                    //filters
                    Optional<String> objectName = query.getFilter();
                    HashMap<String, String> filters = null;
                    try {
                        if (objectName.isPresent() && !objectName.get().trim().isEmpty()) {
                            filters = new HashMap<>();
                            filters.put(Constants.PROPERTY_NAME, objectName.get());
                        }
                        return ss.getTemplateDataSrcCount(filters);

                    } catch (ApplicationObjectNotFoundException | InvalidArgumentException |
                             UnsupportedPropertyException ex) {
                        return 0;
                    }
                }
        );
        cmbDataSourceType.setDataProvider(commonPropertiesDataProvider);
    }

    /**
     * Data source common property grid
     *
     * @param grdDataSourceCommons        : grid of commons properties
     * @param syncDataSourceConfiguration : data source entity
     */
    private void createCommonsGrid(Grid<ParameterItemDataSource> grdDataSourceCommons, SyncDataSourceConfiguration syncDataSourceConfiguration) {
        Editor<ParameterItemDataSource> editor = grdDataSourceCommons.getEditor();
        binderCommonParemers = new Binder<>();
        TextField txtPropertyName = new TextField();
        TextField txtPropertyValue = new TextField();
        ValidationMessage propertyNameValidationMessage = new ValidationMessage();
        ValidationMessage propertyValueValidationMessage = new ValidationMessage();

        editor.setBinder(binderCommonParemers);
        Grid.Column<ParameterItemDataSource> propertyNameColumn = grdDataSourceCommons.addColumn(ParameterItemDataSource::getPropertyName)
                .setHeader(ts.getTranslatedString("module.sync.new.data-source.grid.property.name"))
                .setAutoWidth(true)
                .setResizable(true);

        Grid.Column<ParameterItemDataSource> propertyValueColumn = grdDataSourceCommons.addColumn(ParameterItemDataSource::getPropertyValue)
                .setHeader(ts.getTranslatedString("module.sync.new.data-source.grid.property.value"))
                .setAutoWidth(true)
                .setResizable(true);

        Grid.Column<ParameterItemDataSource> optionsColumn = grdDataSourceCommons.addColumn(
                new ComponentRenderer<>(Button::new, (button, parameter) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR,
                            ButtonVariant.LUMO_ICON);
                    button.addClickListener(e -> {
                        this.removeCommonProperty(parameter, syncDataSourceConfiguration);
                        grdDataSourceCommons.getDataProvider().refreshAll();
                    });
                    button.setIcon(new Icon(VaadinIcon.TRASH));
                })).setAutoWidth(true).setResizable(true);

        //create grid header
        HeaderRow headerRow = grdDataSourceCommons.prependHeaderRow();
        HorizontalLayout lytGridOptions = new HorizontalLayout();
        Div gridHeader = new Div();
        ActionButton btnAddProperty = new ActionButton(new Icon(VaadinIcon.PLUS));

        btnAddProperty.setClassName("confirm-button");
        btnAddProperty.addThemeVariants(ButtonVariant.LUMO_ICON);
        btnAddProperty.addClickListener(item -> {
            ParameterItemDataSource newItemDataSrc = new ParameterItemDataSource();
            syncDataSourceConfiguration.getCommonParameters().addParameterItem(newItemDataSrc);
            grdDataSourceCommons.setItems(syncDataSourceConfiguration.getCommonParameters().getListOfParameters());
            editor.editItem(newItemDataSrc);
            validateSave(syncDataSourceConfiguration);
        });
        gridHeader.getElement().setProperty("innerHTML", ts.getTranslatedString("module.sync.new.data-source.grid.commons.title.html"));
        lytGridOptions.addAndExpand(gridHeader);
        lytGridOptions.add(btnAddProperty);
        lytGridOptions.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        headerRow.join(propertyNameColumn, propertyValueColumn, optionsColumn).setComponent(lytGridOptions);

        //add binders and editor o row
        txtPropertyName.setRequired(true);
        txtPropertyName.setRequiredIndicatorVisible(true);
        txtPropertyName.setValueChangeMode(ValueChangeMode.EAGER);
        txtPropertyName.setWidthFull();
        txtPropertyName.setPlaceholder(ts.getTranslatedString("module.sync.new.data-source.grid.property.name"));
        addCloseHandler(txtPropertyName, editor);
        propertyNameColumn.setEditorComponent(txtPropertyName);

        txtPropertyValue.setRequired(true);
        txtPropertyValue.setRequiredIndicatorVisible(true);
        txtPropertyValue.setValueChangeMode(ValueChangeMode.EAGER);
        txtPropertyValue.setWidthFull();
        txtPropertyValue.setPlaceholder(ts.getTranslatedString("module.sync.new.data-source.grid.property.value"));
        addCloseHandler(txtPropertyValue, editor);
        propertyValueColumn.setEditorComponent(txtPropertyValue);

        binderCommonParemers.forField(txtPropertyName)
                .asRequired(ts.getTranslatedString("error.module.sync.new.data-source.grid.property.name"))
                //.withValidator(item -> item != null && !item.trim().isEmpty(), ts.getTranslatedString("error.module.sync.new.data-source.grid.property.name"))
                .withValidationStatusHandler(
                        handler -> showValidationError(handler, binderCommonParemers
                                , propertyNameValidationMessage
                                , syncDataSourceConfiguration
                        ))
                .bind(ParameterItemDataSource::getPropertyName, ParameterItemDataSource::setPropertyName);
        binderCommonParemers.forField(txtPropertyValue)
                .asRequired(ts.getTranslatedString("error.module.sync.new.data-source.grid.property.value"))
                //.withValidator(item -> item != null && !item.trim().isEmpty(), ts.getTranslatedString("error.module.sync.new.data-source.grid.property.value"))
                .withValidationStatusHandler(
                        handler -> showValidationError(handler, binderCommonParemers
                                , propertyValueValidationMessage
                                , syncDataSourceConfiguration
                        ))
                .bind(ParameterItemDataSource::getPropertyValue, ParameterItemDataSource::setPropertyValue);

        grdDataSourceCommons.addItemDoubleClickListener(e -> {
            editor.editItem(e.getItem());
            com.vaadin.flow.component.Component editorComponent = e.getColumn().getEditorComponent();
            if (editorComponent instanceof Focusable) {
                ((Focusable) editorComponent).focus();
            }
        });

        editor.addCancelListener(e -> {
            propertyNameValidationMessage.setText(null);
            propertyValueValidationMessage.setText(null);
        });

        grdDataSourceCommons.setItems(syncDataSourceConfiguration.getCommonParameters().getParameterToItem());
        binderCommonParemers.addValueChangeListener(event -> {
            validateSave(syncDataSourceConfiguration);
        });
        //add to right layout
        wdwConfigureDataSource.add(grdDataSourceCommons, propertyNameValidationMessage, propertyValueValidationMessage);
    }

    /**
     * Data source specific property grid
     *
     * @param grdDataSourceSpecific       : grid of specific properties
     * @param syncDataSourceConfiguration : data source entity
     */
    private void createSpecificGrid(Grid<ParameterItemDataSource> grdDataSourceSpecific
            , SyncDataSourceConfiguration syncDataSourceConfiguration) {
        Editor<ParameterItemDataSource> editor = grdDataSourceSpecific.getEditor();
        binderSpecificParemers = new Binder<>();
        TextField txtPropertyName = new TextField();
        TextField txtPropertyValue = new TextField();
        ValidationMessage propertyNameValidationMessage = new ValidationMessage();
        ValidationMessage propertyValueValidationMessage = new ValidationMessage();

        editor.setBinder(binderSpecificParemers);
        Grid.Column<ParameterItemDataSource> propertyNameColumn = grdDataSourceSpecific.addColumn(ParameterItemDataSource::getPropertyName)
                .setHeader(ts.getTranslatedString("module.sync.new.data-source.grid.property.name"))
                .setAutoWidth(true)
                .setResizable(true);

        Grid.Column<ParameterItemDataSource> propertyValueColumn = grdDataSourceSpecific.addColumn(ParameterItemDataSource::getPropertyValue)
                .setHeader(ts.getTranslatedString("module.sync.new.data-source.grid.property.value"))
                .setAutoWidth(true)
                .setResizable(true);

        Grid.Column<ParameterItemDataSource> optionsColumn = grdDataSourceSpecific.addColumn(
                new ComponentRenderer<>(Button::new, (button, parameter) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR,
                            ButtonVariant.LUMO_ICON);
                    button.addClickListener(e -> {
                        this.removeSpecificProperty(parameter, syncDataSourceConfiguration);
                        grdDataSourceSpecific.getDataProvider().refreshAll();
                    });
                    button.setIcon(new Icon(VaadinIcon.TRASH));
                })).setAutoWidth(true).setResizable(true);

        //create grid header
        HeaderRow headerRow = grdDataSourceSpecific.prependHeaderRow();
        HorizontalLayout lytGridOptions = new HorizontalLayout();
        Div gridHeader = new Div();
        ActionButton btnAddProperty = new ActionButton(new Icon(VaadinIcon.PLUS));
        //add property
        btnAddProperty.setClassName("confirm-button");
        btnAddProperty.addThemeVariants(ButtonVariant.LUMO_ICON);
        btnAddProperty.addClickListener(item -> {
            ParameterItemDataSource newItemDataSrc = new ParameterItemDataSource();
            syncDataSourceConfiguration.addParameterItem(newItemDataSrc);
            grdDataSourceSpecific.setItems(syncDataSourceConfiguration.getListOfParameters());
            editor.editItem(newItemDataSrc);
            validateSave(syncDataSourceConfiguration);
        });
        gridHeader.getElement().setProperty("innerHTML", ts.getTranslatedString("module.sync.new.data-source.grid.specific.title.html"));
        lytGridOptions.addAndExpand(gridHeader);
        lytGridOptions.add(btnAddProperty);
        lytGridOptions.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        headerRow.join(propertyNameColumn, propertyValueColumn, optionsColumn).setComponent(lytGridOptions);

        txtPropertyName.setRequired(true);
        txtPropertyName.setRequiredIndicatorVisible(true);
        txtPropertyName.setValueChangeMode(ValueChangeMode.EAGER);
        txtPropertyName.setWidthFull();
        txtPropertyName.setPlaceholder(ts.getTranslatedString("module.sync.new.data-source.grid.property.name"));
        addCloseHandler(txtPropertyName, editor);
        propertyNameColumn.setEditorComponent(txtPropertyName);

        txtPropertyValue.setRequired(true);
        txtPropertyValue.setRequiredIndicatorVisible(true);
        txtPropertyValue.setValueChangeMode(ValueChangeMode.EAGER);
        txtPropertyValue.setWidthFull();
        txtPropertyValue.setPlaceholder(ts.getTranslatedString("module.sync.new.data-source.grid.property.value"));
        addCloseHandler(txtPropertyValue, editor);
        propertyValueColumn.setEditorComponent(txtPropertyValue);

        binderSpecificParemers.forField(txtPropertyName)
                .asRequired(ts.getTranslatedString("error.module.sync.new.data-source.grid.property.name"))
                .withValidationStatusHandler(
                        handler -> showValidationError(handler, binderSpecificParemers
                                , propertyNameValidationMessage
                                , syncDataSourceConfiguration
                        ))
                .bind(ParameterItemDataSource::getPropertyName, ParameterItemDataSource::setPropertyName);
        binderSpecificParemers.forField(txtPropertyValue)
                .asRequired(ts.getTranslatedString("error.module.sync.new.data-source.grid.property.value"))
                .withValidationStatusHandler(
                        handler -> showValidationError(handler, binderSpecificParemers
                                , propertyValueValidationMessage
                                , syncDataSourceConfiguration
                        ))
                .bind(ParameterItemDataSource::getPropertyValue, ParameterItemDataSource::setPropertyValue);


        grdDataSourceSpecific.addItemDoubleClickListener(e -> {
            editor.editItem(e.getItem());
            com.vaadin.flow.component.Component editorComponent = e.getColumn().getEditorComponent();
            if (editorComponent instanceof Focusable) {
                ((Focusable) editorComponent).focus();
            }
        });

        editor.addCancelListener(e -> {
            propertyNameValidationMessage.setText(null);
            propertyValueValidationMessage.setText(null);
        });

        grdDataSourceSpecific.setItems(syncDataSourceConfiguration.getParameterToItem());
        //add to right layout
        wdwConfigureDataSource.add(grdDataSourceSpecific, propertyNameValidationMessage, propertyValueValidationMessage);
    }

    /**
     * save data of data source configuration
     *
     * @param syncDataSourceConfiguration data source configuration
     */
    private void saveDatasource(SyncDataSourceConfiguration syncDataSourceConfiguration) {
        validateSave(syncDataSourceConfiguration);
        if (syncDataSourceConfiguration.getCommonParameters() != null
                && syncDataSourceConfiguration.getCommonParameters().getDataSourcetype() != null) {
            try {
                ss.saveDataSource(syncDataSourceConfiguration);

                if (syncDataSourceConfiguration.getId() > 0)
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.information")
                            , ts.getTranslatedString("successfully.module.sync.new.data-source.edit")
                            , AbstractNotification.NotificationType.INFO, ts).open();
                else
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.information")
                            , ts.getTranslatedString("successfully.module.sync.new.data-source.save")
                            , AbstractNotification.NotificationType.INFO, ts).open();

            } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.information")
                        , ex.getMessage()
                        , AbstractNotification.NotificationType.ERROR, ts).open();
            }
        } else {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.information"),
                    ts.getTranslatedString("error.module.sync.new.data-source.properties"),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    private void validateSave(SyncDataSourceConfiguration syncDataSourceConfiguration) {
        boolean valid = syncDataSourceConfiguration.getName() != null && !syncDataSourceConfiguration.getName().trim().isEmpty();
        valid = valid && !syncDataSourceConfiguration.getListOfParameters().isEmpty()
                && syncDataSourceConfiguration.getListOfParameters()
                .stream().allMatch(item -> {
                    return item.getPropertyName() != null && !item.getPropertyName().trim().isEmpty()
                            && item.getPropertyValue() != null && !item.getPropertyValue().trim().isEmpty();
                });
        valid = valid && syncDataSourceConfiguration.getCommonParameters() != null;
        valid = valid && syncDataSourceConfiguration.getCommonParameters().getDataSourcetype() != null;
        valid = valid && !syncDataSourceConfiguration.getCommonParameters().getListOfParameters().isEmpty()
                && syncDataSourceConfiguration.getCommonParameters().getListOfParameters()
                .stream().allMatch(item -> {
                    return item.getPropertyName() != null && !item.getPropertyName().trim().isEmpty()
                            && item.getPropertyValue() != null && !item.getPropertyValue().trim().isEmpty();
                });
        if (verifiedHandler != null)
            valid = valid && !verifiedHandler.isError();

        if (valid) {
            wdwConfigureDataSource.getBtnConfirm().setEnabled(true);
            wdwConfigureDataSource.getBtnConfirm().setClassName("confirm-button");
            wdwConfigureDataSource.getBtnConfirm().addThemeVariants(ButtonVariant.LUMO_LARGE
                    , ButtonVariant.LUMO_PRIMARY);
        } else {
            wdwConfigureDataSource.getBtnConfirm().setEnabled(false);
            wdwConfigureDataSource.getBtnConfirm().removeClassName("confirm-button");
            wdwConfigureDataSource.getBtnConfirm().removeThemeVariants(ButtonVariant.LUMO_LARGE
                    , ButtonVariant.LUMO_PRIMARY);
        }
    }

    // Convenience method for showing the validation error in properties
    private void showValidationError(BindingValidationStatus<?> handler
            , Binder<ParameterItemDataSource> binder
            , ValidationMessage propertyValueValidationMessage
            , SyncDataSourceConfiguration syncDataSourceConfiguration) {
        if (binder.getBean() != null && handler.isError())
            propertyValueValidationMessage.setText(handler.getMessage().get());
        else
            propertyValueValidationMessage.setText(null);

        verifiedHandler = handler;
        validateSave(syncDataSourceConfiguration);
    }

    private void removeSpecificProperty(ParameterItemDataSource parameter, SyncDataSourceConfiguration syncDataSourceConfiguration) {
        syncDataSourceConfiguration.getParameterToItem().remove(parameter);
    }

    private void removeCommonProperty(ParameterItemDataSource parameter, SyncDataSourceConfiguration syncDataSourceConfiguration) {
        syncDataSourceConfiguration.getCommonParameters().getParameterToItem().remove(parameter);
    }

    private TemplateRenderer<TemplateDataSource> getRenderer() {
        StringBuilder tpl = new StringBuilder();
        tpl.append("<div style=\"display: flex;\">");
        tpl.append("    [[item.name]] - ");
        tpl.append("    <div style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">[[[item.description]]]</div>");
        tpl.append("  </div>");
        tpl.append("</div>");

        return TemplateRenderer.<TemplateDataSource>of(tpl.toString())
                .withProperty("name", TemplateDataSource::getName)
                .withProperty("description", TemplateDataSource::getDescription);
    }

    @Override
    public AbstractAction getModuleAction() {
        return newSyncDataSourceConfigurationAction;
    }

    /**
     * refresh grid
     *
     * @return commandClose;Command; refresh action
     */
    public Command getCommandClose() {
        return commandClose;
    }

    /**
     * @param commandClose;Command; refresh action
     */
    public void setCommandClose(Command commandClose) {
        this.commandClose = commandClose;
    }
}