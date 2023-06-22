/*
 * Copyright 2023-2023. Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.modules.commercial.sync.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.server.Command;
import lombok.Getter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.UnsupportedPropertyException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.sync.SynchronizationService;
import org.neotropic.kuwaiba.modules.commercial.sync.actions.DeleteSynchronizationGroupVisualAction;
import org.neotropic.kuwaiba.modules.commercial.sync.actions.NewSyncGroupVisualAction;
import org.neotropic.kuwaiba.modules.commercial.sync.actions.ReleaseSyncDataSourceConfigurationVisualAction;
import org.neotropic.kuwaiba.modules.commercial.sync.actions.RunSynchronizationVisualAction;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncDataSourceConfiguration;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SynchronizationGroup;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@neotropic.co>}
 */
public class SyncGroupTab extends Tab {

    /**
     * Parameter, group to be released from datasource.
     */
    public static String PARAM_RELEASE_GROUP = "groups"; //NOI18N
    /**
     * Parameter, group to be released from datasource.
     */
    public static String PARAM_SYNC_GROUP = "group"; //NOI18N
    /**
     * Parameter, data source configuration.
     */
    public static String PARAM_SYNC_DATA_SOURCE = "syncDataSourceConfiguration"; //NOI18N
    /**
     * Parameter command close.
     */
    public static String PARAM_COMMANDCLOSE = "commandClose";
    /**
     * Icon size.
     */
    public static String ICON_SIZE = "32px";
    @Getter
    private final VerticalLayout tabContent;
    /**
     * The visual action for delete Sync Data group
     */
    private final DeleteSynchronizationGroupVisualAction deleteSynchronizationGroupVisualAction;
    /**
     * The visual action for release Sync Data Source Configuration
     */
    private final ReleaseSyncDataSourceConfigurationVisualAction releaseSyncDataSourceConfigurationVisualAction;
    /**
     * The visual action for release Sync Data Source Configuration
     */
    private final NewSyncGroupVisualAction newSyncGroupVisualAction;
    /**
     * The visual action for run Synchronization
     */
    private final RunSynchronizationVisualAction runSynchronizationVisualAction;
    /**
     * Reference to the Business Entity Manager
     */
    private final BusinessEntityManager bem;
    /**
     * Reference to the Translation Service
     */
    private final TranslationService ts;
    /**
     * Reference to the Synchronization Service
     */
    private final SynchronizationService ss;
    /**
     * Layouts
     */
    private VerticalLayout lytRightSide;
    private VerticalLayout lytLeftSide;
    private ActionButton btnManagePools;
    private ActionButton btnDeletePools;
    private ActionButton btnRunPools;
    private Button btnSave;
    private ComboBox<SyncDataSourceConfiguration> cmbDataSources;
    /**
     * Content elements
     * Object to filter for pool name
     */
    private ComboBox<SynchronizationGroup> cmbSynGroups;
    /**
     * The grid with sync data source configurations list
     */
    private Grid<SyncDataSourceConfiguration> gridSourceConfigurations;
    /**
     * Object to saveDataSource the selected sync group
     */
    private SynchronizationGroup selectedGroup;
    /**
     * Object to saveDataSource the selected sync data source configuration
     */
    private SyncDataSourceConfiguration currentConfiguration;
    /**
     * Configuration name
     */
    private Label lblConfigurationName;
    /**
     * Split the content
     */
    private HorizontalLayout mainLayout;

    public SyncGroupTab(String name, boolean enabled, boolean selectedTab
            , DeleteSynchronizationGroupVisualAction deleteSynchronizationGroupVisualAction
            , ReleaseSyncDataSourceConfigurationVisualAction releaseSyncDataSourceConfigurationVisualAction
            , NewSyncGroupVisualAction newSyncGroupVisualAction
            , RunSynchronizationVisualAction runSynchronizationVisualAction
            , BusinessEntityManager bem
            , SynchronizationService ss
            , TranslationService ts) {
        super(name);
        setEnabled(enabled);
        setSelected(selectedTab);
        this.tabContent = new VerticalLayout();
        this.deleteSynchronizationGroupVisualAction = deleteSynchronizationGroupVisualAction;
        this.releaseSyncDataSourceConfigurationVisualAction = releaseSyncDataSourceConfigurationVisualAction;
        this.newSyncGroupVisualAction = newSyncGroupVisualAction;
        this.runSynchronizationVisualAction = runSynchronizationVisualAction;
        this.bem = bem;
        this.ss = ss;
        this.ts = ts;
        initElements();
        //by default it will be invisible, state change will be maked by parent class
        this.tabContent.setVisible(false);
        add(tabContent);
    }

    private void initElements() {

        tabContent.removeAll();
        tabContent.setSizeFull();
        tabContent.setMargin(false);
        tabContent.setSpacing(false);
        tabContent.setPadding(false);

        // Main Layout
        //Left side | Right side
        mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        // create Left side
        createLeftSide();
        lytRightSide = new VerticalLayout();
        lytRightSide.setVisible(false);

        tabContent.add(mainLayout);
    }


    private void createLeftSide() {
        btnManagePools = new ActionButton(new ActionIcon(VaadinIcon.COG),
                ts.getTranslatedString("module.sync.actions.get-sync-group.name"));
        ActionButton btnAddPools = new ActionButton(new ActionIcon(VaadinIcon.PLUS),
                ts.getTranslatedString("module.sync.actions.new-sync-group.description"));
        btnDeletePools = new ActionButton(new ActionIcon(VaadinIcon.TRASH),
                this.deleteSynchronizationGroupVisualAction.getModuleAction().getDisplayName());
        btnRunPools = new ActionButton(new ActionIcon(VaadinIcon.PLAY_CIRCLE),
                ts.getTranslatedString("module.sync.actions.get-sync-group.description"));

        // Main left Layout 
        lytLeftSide = new VerticalLayout();
        lytLeftSide.setClassName("left-side");
        lytLeftSide.setSizeFull();
        lytLeftSide.setMargin(false);
        lytLeftSide.setSpacing(false);
        lytLeftSide.setId("left-lyt");
        lytLeftSide.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);

        //left side components       
        btnRunPools.addThemeVariants(ButtonVariant.LUMO_ICON);
        btnRunPools.setEnabled(false);
        btnRunPools.setHeight(ICON_SIZE);
        btnRunPools.addClickListener(event -> {
            if (selectedGroup.getSyncDataSourceConfigurations() != null && !selectedGroup.getSyncDataSourceConfigurations().isEmpty()) {
                this.runSynchronizationVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(PARAM_SYNC_GROUP, selectedGroup)
                )).open();
            } else
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error")
                        , ts.getTranslatedString("error.module.sync.new.data-source.empty")
                        , AbstractNotification.NotificationType.ERROR, ts).open();
        });

        btnAddPools.setHeight(ICON_SIZE);
        btnAddPools.setClassName("confirm-button");
        btnAddPools.addThemeVariants(ButtonVariant.LUMO_ICON);
        btnAddPools.addClickListener(event -> createRightSide(new SynchronizationGroup()));

        Command deleteConfiguration = () -> {
            cmbSynGroups.getDataProvider().refreshAll();
            cmbSynGroups.setValue(null);
            createGridDataSource();
        };
        btnDeletePools.setEnabled(false);
        btnDeletePools.addThemeVariants(ButtonVariant.LUMO_ICON);
        btnDeletePools.setHeight(ICON_SIZE);
        btnDeletePools.addClickListener(event -> {
            this.deleteSynchronizationGroupVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter(PARAM_RELEASE_GROUP, selectedGroup),
                    new ModuleActionParameter(PARAM_COMMANDCLOSE, deleteConfiguration)
            )).open();
        });

        btnManagePools.addThemeVariants(ButtonVariant.LUMO_ICON);
        btnManagePools.setEnabled(false);
        btnManagePools.setHeight(ICON_SIZE);
        btnManagePools.addClickListener(event -> createRightSide(selectedGroup));

        // Layout for combo box
        createComboGroups();
        HorizontalLayout lytCmb = new HorizontalLayout();
        lytCmb.setWidthFull();
        lytCmb.setMargin(false);
        lytCmb.setPadding(false);
        lytCmb.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytCmb.addAndExpand(cmbSynGroups);
        lytCmb.add(btnManagePools, btnAddPools, btnDeletePools, btnRunPools);
        lytCmb.setAlignItems(FlexComponent.Alignment.END);
        lytLeftSide.add(lytCmb);
        createSyncConfigurationsGrid();
        //add to main layout
        mainLayout.add(lytLeftSide);
    }

    private void createRightSide(SynchronizationGroup syncGroup) {
        lytRightSide.removeAll();
        if (syncGroup != null) {
            this.newSyncGroupVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter(PARAM_SYNC_GROUP, syncGroup)
            )).open();

        }
    }

    private void createComboGroups() {
        // First filter
        cmbSynGroups = new ComboBox<>(ts.getTranslatedString("module.sync.actions.get-sync-group.filter-header"));
        cmbSynGroups.setPlaceholder(ts.getTranslatedString("module.sync.actions.get-sync-group.filter-label.choose"));
        cmbSynGroups.setWidthFull();
        cmbSynGroups.setAllowCustomValue(false);
        cmbSynGroups.setClearButtonVisible(true);
        cmbSynGroups.setItemLabelGenerator(item -> item.getName() != null ? item.getName() : "");
        cmbSynGroups.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                selectedGroup = event.getValue();
                createGridDataSource();
                toogleButtons(true);
            } else {
                selectedGroup = null;
                toogleButtons(false);
                gridSourceConfigurations.setItems(new ArrayList<>());
            }
            lytRightSide.setVisible(false);
        });
        buildComboBoxFilterProvider();
    }

    /**
     * Create pools data provider
     */
    private void buildComboBoxFilterProvider() {
        DataProvider<SynchronizationGroup, String> commonPropertiesDataProvider = DataProvider.fromFilteringCallbacks(
                query -> {
                    List<SynchronizationGroup> elements;
                    //filters
                    Optional<String> objectName = query.getFilter();
                    HashMap<String, String> filters = null;
                    try {
                        if (objectName.isPresent() && !objectName.get().trim().isEmpty()) {
                            filters = new HashMap<>();
                            filters.put(Constants.PROPERTY_NAME, objectName.get());
                        }
                        elements = ss.getSyncGroups(filters, query.getOffset(), query.getLimit());
                    } catch (UnsupportedPropertyException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                                AbstractNotification.NotificationType.ERROR, ts).open();
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
                        return ss.getSyncGroups(filters);

                    } catch (UnsupportedPropertyException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                                AbstractNotification.NotificationType.ERROR, ts).open();
                        return 0;
                    }
                }
        );
        cmbSynGroups.setDataProvider(commonPropertiesDataProvider);
    }

    private void createSyncConfigurationsGrid() {

        gridSourceConfigurations = new Grid<>();
        gridSourceConfigurations.setHeight(80, Unit.VH);
        gridSourceConfigurations.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        gridSourceConfigurations.setSelectionMode(Grid.SelectionMode.SINGLE);
        gridSourceConfigurations.setWidthFull();

        gridSourceConfigurations.addColumn(SyncDataSourceConfiguration::getName)
                .setHeader(ts.getTranslatedString("module.sync.data-source.grid.name"))
                .setResizable(true);

        gridSourceConfigurations.addColumn(SyncDataSourceConfiguration::getDescription)
                .setHeader(ts.getTranslatedString("module.sync.data-source.grid.description"))
                .setResizable(true);

        gridSourceConfigurations.addColumn(item -> item.getBusinessObjectLight() != null
                        ? item.getBusinessObjectLight().getName() : "")
                .setHeader(ts.getTranslatedString("module.sync.data-source.object.light.label"))
                .setResizable(true);

        gridSourceConfigurations.addComponentColumn(this::addButtonsToGrid)
                .setHeader(ts.getTranslatedString("module.sync.data-source.grid.options"))
                .setResizable(true);

        lytLeftSide.add(gridSourceConfigurations);
    }

    /**
     * options for every data source item
     *
     * @param item A data source configuration item
     * @return Menu with allowed action over data source
     */
    private Component addButtonsToGrid(SyncDataSourceConfiguration item) {

        Icon btnRelease = new Icon(VaadinIcon.UNLINK);

        Command afterExecuteDelete = () -> {
            gridSourceConfigurations.getDataProvider().refreshAll();
            lytRightSide.setVisible(true);
        };
        btnRelease.setColor("var(--lumo-error-text-color)");
        btnRelease.getElement().setProperty("title"
                , ts.getTranslatedString("module.sync.actions.sync-release-sync-data-source-configuration.description"));
        btnRelease.setSize("var(--lumo-icon-size-m)");
        btnRelease.addClickListener(event -> {
            this.releaseSyncDataSourceConfigurationVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter(PARAM_SYNC_DATA_SOURCE, item)
                    , new ModuleActionParameter(PARAM_RELEASE_GROUP, selectedGroup)
                    , new ModuleActionParameter(PARAM_COMMANDCLOSE, afterExecuteDelete)
            )).open();
        });
        return btnRelease;
    }

    private void createGridDataSource() {

        if (selectedGroup != null) {
            ConfigurableFilterDataProvider<SyncDataSourceConfiguration, Void, HashMap<String, String>> cdpDataSource;
            CallbackDataProvider<SyncDataSourceConfiguration, HashMap<String, String>> dataSourceProvider = DataProvider.fromFilteringCallbacks(
                    query -> {
                        List<SyncDataSourceConfiguration> elements;
                        //filters
                        HashMap<String, String> filters = query.getFilter().orElse(null);
                        try {
                            elements = ss.getSyncDataSrcBySyncGroupId(selectedGroup.getId(), filters, query.getOffset(), query.getLimit());
                        } catch (ApplicationObjectNotFoundException | InvalidArgumentException |
                                 UnsupportedPropertyException ex) {
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                                    AbstractNotification.NotificationType.ERROR, ts).open();
                            elements = new ArrayList<>();
                        }
                        return elements.stream();
                    },
                    query -> {
                        //filters
                        HashMap<String, String> filters = query.getFilter().orElse(null);
                        try {
                            return ss.getSyncDataSrcBySyncGroupIdCount(selectedGroup.getId(), filters);
                        } catch (ApplicationObjectNotFoundException | InvalidArgumentException |
                                 UnsupportedPropertyException ex) {
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                                    AbstractNotification.NotificationType.ERROR, ts).open();
                            return 0;
                        }
                    }
            );
            cdpDataSource = dataSourceProvider.withConfigurableFilter();
            gridSourceConfigurations.setDataProvider(cdpDataSource);
        } else {
            gridSourceConfigurations.setItems(new ArrayList<>());
        }
    }

    /**
     * Shows/Hides the labels and buttons in the header, also the property sheet.
     */
    private void toogleButtons(boolean show) {
        btnManagePools.setEnabled(show);
        btnDeletePools.setEnabled(show);
        btnRunPools.setEnabled(show);
        if (show) {
            btnManagePools.setClassName("confirm-button");
            btnDeletePools.addThemeVariants(ButtonVariant.LUMO_ERROR);
            btnRunPools.setClassName("confirm-button");
        } else {
            btnManagePools.removeClassName("confirm-button");
            btnDeletePools.removeThemeVariants(ButtonVariant.LUMO_ERROR);
            btnRunPools.removeClassName("confirm-button");
        }
    }
}
