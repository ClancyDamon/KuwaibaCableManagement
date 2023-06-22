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

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.UnsupportedPropertyException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.sync.SynchronizationModule;
import org.neotropic.kuwaiba.modules.commercial.sync.SynchronizationService;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncDataSourceConfiguration;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SynchronizationGroup;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * Visual wrapper of release Synchronization Data Source Configuration action.
 *
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class ReleaseSyncDataSourceConfigurationVisualAction extends AbstractVisualAction<Dialog> {

    /**
     * Parameter, group to be released from data source.
     */
    public static String PARAM_RELEASE_GROUP = "groups"; //NOI18N
    /**
     * Parameter, data source configuration.
     */
    public static String PARAM_SYNC_DATA_SOURCE = "syncDataSourceConfiguration"; //NOI18N
    /**
     * Parameter exception.
     */
    public static String PARAM_EXCEPTION = "exception"; //NOI18N
    /**
     * Parameter command close.
     */
    public static String PARAM_COMMANDCLOSE = "commandClose"; //NOI18N
    /**
     * Close action command
     */
    private Command commandClose;
    /**
     * Object to validate
     */
    private Boolean success = true;
    /**
     * Reference to the Synchronization Service
     */
    @Autowired
    private SynchronizationService ss;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private ReleaseSyncDataSourceConfigurationAction releaseSyncDataSourceConfigurationAction;

    public ReleaseSyncDataSourceConfigurationVisualAction() {
        super(SynchronizationModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        SyncDataSourceConfiguration configuration;

        if (parameters.containsKey(PARAM_SYNC_DATA_SOURCE)) {
            configuration = (SyncDataSourceConfiguration) parameters.get(PARAM_SYNC_DATA_SOURCE);
            commandClose = (Command) parameters.get(PARAM_COMMANDCLOSE);
            SynchronizationGroup parentGoup = (SynchronizationGroup) parameters.get(PARAM_RELEASE_GROUP);

            ConfirmDialog wdwReleaseConfiguration = new ConfirmDialog(ts,
                    this.releaseSyncDataSourceConfigurationAction.getDisplayName(),
                    String.format(ts.getTranslatedString("module.sync.actions.sync-release-sync-data-source-configuration.confirm"),
                            configuration.getName()),
                    ts.getTranslatedString("module.sync.actions.sync-release-sync-data-source-configuration.dialog.btn.accept"));

            wdwReleaseConfiguration.setDraggable(true);
            wdwReleaseConfiguration.setResizable(true);
            if (parentGoup == null) {
                wdwReleaseConfiguration.getBtnConfirm().setEnabled(false);
                // dialog body
                MultiSelectListBox<SynchronizationGroup> listGroups = new MultiSelectListBox<>();
                Icon iconSearch = VaadinIcon.SEARCH.create();
                TextField txtSearchClass = new TextField();
                try {
                    List<SynchronizationGroup> groups = ss.getSyncGroupsFromDataSource(configuration.getId(), null, -1, -1);
                    // Search
                    iconSearch.getElement().setProperty("title", ts.getTranslatedString("module.sync.actions.get-sync-group.filter-label.find"));
                    iconSearch.setSize("16px");
                    txtSearchClass.setWidthFull();
                    txtSearchClass.setPlaceholder(ts.getTranslatedString("module.general.label.search-by-name"));
                    txtSearchClass.setClearButtonVisible(true);
                    txtSearchClass.setSuffixComponent(iconSearch);
                    txtSearchClass.setValueChangeMode(ValueChangeMode.EAGER);
                    // Find
                    txtSearchClass.addValueChangeListener(event -> {
                        String value = event.getValue();
                        try {
                            if (value != null && !value.isEmpty()) {
                                HashMap<String, String> filters = new HashMap<>();
                                filters.put(Constants.PROPERTY_NAME, value);
                                listGroups.setItems(ss.getSyncGroupsFromDataSource(configuration.getId(), filters, -1, -1));
                            } else {
                                listGroups.setItems(ss.getSyncGroupsFromDataSource(configuration.getId(), null, -1, -1));
                            }

                        } catch (InvalidArgumentException | MetadataObjectNotFoundException |
                                 UnsupportedPropertyException ex) {
                            fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                                    ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                    ex.getMessage(), DeleteSyncDataSourceConfigurationAction.class));
                        }
                    });
                    // Sync group list
                    listGroups.setItems(groups);
                    listGroups.setRenderer(new ComponentRenderer<>(item -> {
                        return new Label(item.getName());
                    }));
                    listGroups.addValueChangeListener(event -> {
                        wdwReleaseConfiguration.getBtnConfirm().setEnabled(!listGroups.getSelectedItems().isEmpty());
                    });
                    //add to dialog
                    wdwReleaseConfiguration.setContent(txtSearchClass, listGroups);
                } catch (InvalidArgumentException | MetadataObjectNotFoundException | UnsupportedPropertyException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                            ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), DeleteSyncDataSourceConfigurationAction.class));
                }
                wdwReleaseConfiguration.getBtnConfirm().addClickListener((event) -> {
                    Long[] arrayGroups = listGroups.getSelectedItems().stream().map(item -> item.getId()).toArray(size -> new Long[size]);
                    try {
                        ActionResponse actionResponse
                                = releaseSyncDataSourceConfigurationAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>(PARAM_RELEASE_GROUP, arrayGroups),
                                new ModuleActionParameter<>(PARAM_SYNC_DATA_SOURCE, configuration)));

                        if (actionResponse.containsKey(PARAM_EXCEPTION)) {
                            success = false;
                            throw new ModuleActionException(((Exception) actionResponse.get(PARAM_EXCEPTION)).getLocalizedMessage());
                        }
                    } catch (ModuleActionException ex) {
                        success = false;
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                                ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                ex.getMessage(), DeleteSyncDataSourceConfigurationAction.class));
                    }
                    wdwReleaseConfiguration.close();

                    if (success == true) {
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                                ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.sync.actions.sync-release-sync-data-source-configuration.success"),
                                DeleteSyncDataSourceConfigurationAction.class));
                        getCommandClose().execute();
                    }
                });
            } else {
                Div dialogMessage = new Div();
                dialogMessage.getElement().setProperty("innerHTML"
                        , String.format(ts.getTranslatedString("module.sync.actions.sync-release-sync-data-source-configuration.confirm"), parentGoup.getName()));
                wdwReleaseConfiguration.setContent(dialogMessage);
                wdwReleaseConfiguration.getBtnConfirm().addClickListener((event) -> {
                    Long[] arrayGroups = {parentGoup.getId()};
                    try {
                        ActionResponse actionResponse
                                = releaseSyncDataSourceConfigurationAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>(PARAM_RELEASE_GROUP, arrayGroups),
                                new ModuleActionParameter<>(PARAM_SYNC_DATA_SOURCE, configuration)));

                        if (actionResponse.containsKey(PARAM_EXCEPTION)) {
                            success = false;
                            throw new ModuleActionException(((Exception) actionResponse.get(PARAM_EXCEPTION)).getLocalizedMessage());
                        }
                    } catch (ModuleActionException ex) {
                        success = false;
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                                ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                ex.getMessage(), DeleteSyncDataSourceConfigurationAction.class));
                    }
                    wdwReleaseConfiguration.close();

                    if (success == true) {
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                                ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.sync.actions.sync-release-sync-data-source-configuration.success"),
                                DeleteSyncDataSourceConfigurationAction.class));
                        getCommandClose().execute();
                    }
                });
            }
            return wdwReleaseConfiguration;
        } else {
            return new Dialog(new Label(ts.getTranslatedString("module.sync.actions.sync-release-sync-data-source-configuration.error")));
        }
    }

    @Override
    public AbstractAction getModuleAction() {
        return releaseSyncDataSourceConfigurationAction;
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
