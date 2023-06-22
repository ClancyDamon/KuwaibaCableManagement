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

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.UnsupportedPropertyException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.sync.SynchronizationModule;
import org.neotropic.kuwaiba.modules.commercial.sync.SynchronizationService;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncDataSourceConfiguration;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SynchronizationGroup;
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
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@neotropic.co>}
 */
@Component
public class NewSyncGroupVisualAction extends AbstractVisualAdvancedAction {

    /**
     * Icon size.
     */
    public static String ICON_SIZE = "32px";
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
    private ComboBox<SyncDataSourceConfiguration> cmbDataSources;
    private SynchronizationGroup selectedObject;
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
    private NewSyncGroupAction newSyncGroupAction;
    /**
     * Close action command
     */
    private Command commandClose;
    /**
     * Window to configure sync data source
     */
    private ConfirmDialog wdwConfigureDataSource;

    public NewSyncGroupVisualAction() {
        super(SynchronizationModule.MODULE_ID);
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

        if (parameters.containsKey(PARAM_GROUP)) {
            selectedObject = (SynchronizationGroup) parameters.get(PARAM_GROUP);
            commandClose = (Command) parameters.get(PARAM_COMMANDCLOSE);

            wdwConfigureDataSource = new ConfirmDialog(ts
                    , ts.getTranslatedString("module.sync.actions.get-sync-group.name")
                    , ts.getTranslatedString("module.sync.data-source.button.accept")
            );
            wdwConfigureDataSource.getBtnConfirm().setEnabled(false);
            wdwConfigureDataSource.setResizable(true);
            wdwConfigureDataSource.setDraggable(true);
            createDialogContent();

            return wdwConfigureDataSource;
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.general.messages.object-not-found")));
    }

    private void createDialogContent() {

        HorizontalLayout lytSynGroupHeader = new HorizontalLayout();
        HorizontalLayout lytDataSourcer = new HorizontalLayout();

        ActionButton btnAddDataSource = new ActionButton(new Icon(VaadinIcon.PLUS)
                , ts.getTranslatedString("module.sync.actions.new-sync-data-source-configuration.description"));
        btnAddDataSource.setHeight(ICON_SIZE);
        TextField txtSynGroupName = new TextField(ts.getTranslatedString("module.sync.actions.get-sync-group.text.name.title"));
        TextArea txaSynGroupDescription = new TextArea(ts.getTranslatedString("module.sync.actions.get-sync-group.text.description.title"));
        cmbDataSources = new ComboBox<>(ts.getTranslatedString("module.sync.data-source.title"));
        Grid<SyncDataSourceConfiguration> grdTemporalDatasource = new Grid<>();

        txtSynGroupName.setPlaceholder(ts.getTranslatedString("module.sync.actions.get-sync-group.text.name.placeholder"));
        txtSynGroupName.setWidthFull();
        txtSynGroupName.setRequired(true);
        txtSynGroupName.setRequiredIndicatorVisible(true);
        txtSynGroupName.setValueChangeMode(ValueChangeMode.EAGER);
        txtSynGroupName.addValueChangeListener(event -> {
            selectedObject.setName(event.getValue());
            validateSave();
        });

        txaSynGroupDescription.setPlaceholder(ts.getTranslatedString("module.sync.actions.get-sync-group.text.description.placeholder"));
        txaSynGroupDescription.setWidthFull();
        txaSynGroupDescription.setRequired(true);
        txaSynGroupDescription.setRequiredIndicatorVisible(true);
        txaSynGroupDescription.setValueChangeMode(ValueChangeMode.EAGER);
        txaSynGroupDescription.addValueChangeListener(event -> {
            selectedObject.setDescription(event.getValue());
            validateSave();
        });

        btnAddDataSource.addClassName("confirm-button");
        btnAddDataSource.addThemeVariants(ButtonVariant.LUMO_ICON);
        btnAddDataSource.addClickListener(event -> {
            if (cmbDataSources.getValue() != null) {
                selectedObject.addSyncDataSource(cmbDataSources.getValue());
                grdTemporalDatasource.getDataProvider().refreshAll();
            }
        });

        wdwConfigureDataSource.getBtnConfirm().addClickListener(event -> {
            try {
                ActionResponse actionResponse = newSyncGroupAction.getCallback()
                        .execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>(PARAM_GROUP, selectedObject)));

                if (actionResponse.containsKey(PARAM_EXCEPTION)) {
                    throw new ModuleActionException(((Exception) actionResponse.get(PARAM_EXCEPTION)).getLocalizedMessage());
                }
                //refresh related grid
                if (getCommandClose() != null) {
                    getCommandClose().execute();
                }

                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                        ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                        ts.getTranslatedString("successfully.module.sync.new.group.save"),
                        DeleteSynchronizationGroupAction.class));
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                        ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), DeleteSynchronizationGroupAction.class));
            }
            wdwConfigureDataSource.close();
        });

        cmbDataSources.setItemLabelGenerator(item -> item.getName() != null ? item.getName() : "");
        cmbDataSources.setRenderer(getRenderer());
        createCbmDataSourcesProvider();
        createGrdTemporalDatasource(grdTemporalDatasource, selectedObject);
        //binder general properties 
        if (selectedObject.getName() != null)
            txtSynGroupName.setValue(selectedObject.getName());
        if (selectedObject.getDescription() != null)
            txaSynGroupDescription.setValue(selectedObject.getDescription());

        //create right layout
        lytSynGroupHeader.addAndExpand(txtSynGroupName);
        lytDataSourcer.addAndExpand(cmbDataSources);
        lytDataSourcer.add(btnAddDataSource);
        lytSynGroupHeader.setVerticalComponentAlignment(FlexComponent.Alignment.END, btnAddDataSource);
        wdwConfigureDataSource.add(lytSynGroupHeader, txaSynGroupDescription, lytDataSourcer, grdTemporalDatasource);

        validateSave();
    }

    /**
     * data source common properties provider
     */
    private void createCbmDataSourcesProvider() {
        DataProvider<SyncDataSourceConfiguration, String> commonDataSourcesProvider = DataProvider.fromFilteringCallbacks(
                query -> {
                    List<SyncDataSourceConfiguration> elements;
                    //filters
                    Optional<String> objectName = query.getFilter();
                    HashMap<String, String> filters = null;
                    try {
                        if (objectName.isPresent() && !objectName.get().trim().isEmpty()) {
                            filters = new HashMap<>();
                            filters.put(Constants.PROPERTY_NAME, objectName.get());
                        }
                        elements = ss.getSyncDataSrc(filters, query.getOffset(), query.getLimit());
                    } catch (ApplicationObjectNotFoundException | InvalidArgumentException |
                             UnsupportedPropertyException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                                ex.getMessage(),
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
                        return ss.getSyncDataSrcCount(filters);

                    } catch (ApplicationObjectNotFoundException | InvalidArgumentException |
                             UnsupportedPropertyException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                                ex.getMessage(),
                                AbstractNotification.NotificationType.ERROR, ts).open();
                        return 0;
                    }
                }
        );
        cmbDataSources.setDataProvider(commonDataSourcesProvider);
    }

    private void createGrdTemporalDatasource(Grid<SyncDataSourceConfiguration> grdTemporalDatasource
            , SynchronizationGroup synchronizationGroup) {

        try {
            List<SyncDataSourceConfiguration> syncDataSrcs = ss.getSyncDataSrcBySyncGroupId(synchronizationGroup.getId(), null, -1, -1);
            synchronizationGroup.setSyncDataSourceConfigurations(syncDataSrcs);
        } catch (InvalidArgumentException | ApplicationObjectNotFoundException | UnsupportedPropertyException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
        grdTemporalDatasource.setWidthFull();
        grdTemporalDatasource.setHeight("10em");
        grdTemporalDatasource.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT,
                GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
        grdTemporalDatasource.setSelectionMode(Grid.SelectionMode.NONE);

        grdTemporalDatasource.addColumn(SyncDataSourceConfiguration::getName)
                .setHeader(ts.getTranslatedString("module.sync.data-source.grid.name"))
                .setResizable(true);

        grdTemporalDatasource.addColumn(item -> item.getBusinessObjectLight() != null
                        ? item.getBusinessObjectLight().getName() : "")
                .setHeader(ts.getTranslatedString("module.sync.data-source.object.light.label"))
                .setResizable(true);

        grdTemporalDatasource.addComponentColumn(item -> {
                    Icon btnRelease = new Icon(VaadinIcon.TRASH);

                    btnRelease.setColor("var(--lumo-error-text-color)");
                    btnRelease.setSize("var(--lumo-icon-size-m)");
                    btnRelease.getElement().setProperty("title",
                            ts.getTranslatedString("module.sync.actions.sync-release-sync-data-source-configuration.description"));
                    btnRelease.addClickListener(event -> {
                        synchronizationGroup.removeSyncDataSource(item);
                        grdTemporalDatasource.getDataProvider().refreshAll();
                    });
                    return btnRelease;
                }).setHeader(ts.getTranslatedString("module.sync.data-source.grid.options"))
                .setResizable(true);

        DataProvider<SyncDataSourceConfiguration, Void> dataProvider = DataProvider.fromFilteringCallbacks(
                query -> synchronizationGroup.getSyncDataSourceConfigurations().subList(query.getOffset(), query.getLimit()).stream()
                ,
                query -> {
                    if (synchronizationGroup.getSyncDataSourceConfigurations() != null)
                        return synchronizationGroup.getSyncDataSourceConfigurations().size();
                    return 0;
                }
        );
        grdTemporalDatasource.setDataProvider(dataProvider);
    }

    private void validateSave() {

        boolean valid = selectedObject.getName() != null && !selectedObject.getName().trim().isEmpty();
        valid = valid && selectedObject.getDescription() != null && !selectedObject.getDescription().trim().isEmpty();

        if (valid) {
            wdwConfigureDataSource.getBtnConfirm().setEnabled(true);
            wdwConfigureDataSource.getBtnConfirm().addThemeVariants(ButtonVariant.LUMO_LARGE
                    , ButtonVariant.LUMO_PRIMARY);
            wdwConfigureDataSource.getBtnConfirm().addClassName("confirm-button");
        } else {
            wdwConfigureDataSource.getBtnConfirm().setEnabled(false);
            wdwConfigureDataSource.getBtnConfirm().removeThemeVariants(ButtonVariant.LUMO_LARGE
                    , ButtonVariant.LUMO_PRIMARY);
            wdwConfigureDataSource.getBtnConfirm().removeClassName("confirm-button");
        }

    }

    private TemplateRenderer<SyncDataSourceConfiguration> getRenderer() {
        StringBuilder tpl = new StringBuilder();
        tpl.append("<div style=\"display: flex;\">");
        tpl.append("    [[item.name]] ");
        tpl.append("    -  <div style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">[[[item.inventoryObject]]]</div>");
        tpl.append("  </div>");
        tpl.append("</div>");

        return TemplateRenderer.<SyncDataSourceConfiguration>of(tpl.toString())
                .withProperty("name", SyncDataSourceConfiguration::getName)
                .withProperty("inventoryObject", item -> item.getBusinessObjectLight().getName());
    }

    @Override
    public AbstractAction getModuleAction() {
        return newSyncGroupAction;
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