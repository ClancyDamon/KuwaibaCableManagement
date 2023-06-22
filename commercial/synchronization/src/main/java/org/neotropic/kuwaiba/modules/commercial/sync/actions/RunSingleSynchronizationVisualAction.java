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

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBox.ItemFilter;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.sync.SynchronizationModule;
import org.neotropic.kuwaiba.modules.commercial.sync.SynchronizationService;
import org.neotropic.kuwaiba.modules.commercial.sync.model.AbstractSyncProvider;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncDataSourceConfiguration;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of release Synchronization Data Source Configuration action.
 *
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class RunSingleSynchronizationVisualAction extends AbstractVisualAction<Dialog> {

    /**
     * Parameter, group to be released from datasource.
     */
    public static String PARAM_RELEASE_GROUP = "groups"; //NOI18N
    /**
     * Parameter, synchonization provider
     */
    public static String PARAM_SYNC_PROVIDER = "syncProvider"; //NOI18N
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
    private RunSynchronizationAction runSynchronizationAction;

    public RunSingleSynchronizationVisualAction() {
        super(SynchronizationModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        SyncDataSourceConfiguration configuration;

        if (parameters.containsKey(PARAM_SYNC_DATA_SOURCE)) {
            configuration = (SyncDataSourceConfiguration) parameters.get(PARAM_SYNC_DATA_SOURCE);
            commandClose = (Command) parameters.get(PARAM_COMMANDCLOSE);

            ConfirmDialog wdwReleaseConfiguration = new ConfirmDialog(ts,
                    ts.getTranslatedString("module.sync.data-source.button.run.description"),
                    ts.getTranslatedString("module.sync.data-source.button.run"));
            wdwReleaseConfiguration.getBtnConfirm().setEnabled(false);
            wdwReleaseConfiguration.setDraggable(true);
            wdwReleaseConfiguration.setResizable(true);
            wdwReleaseConfiguration.getBtnConfirm().setEnabled(false);
            // dialog body
            ComboBox<AbstractSyncProvider> cmbSyncProvider = new ComboBox<>("Sync Provider");
            cmbSyncProvider.setRequired(true);
            cmbSyncProvider.setRequiredIndicatorVisible(true);
            cmbSyncProvider.setRequiredIndicatorVisible(true);
            cmbSyncProvider.setWidthFull();
            try {
                ItemFilter<AbstractSyncProvider> filter = (syncProvider, filterString) ->
                        syncProvider.getDisplayName().toLowerCase().startsWith(filterString.toLowerCase());
                //sync provider list
                cmbSyncProvider.setItems(filter, ss.getAllProviders(null));
                cmbSyncProvider.setItemLabelGenerator(item -> item.getDisplayName() != null ? item.getDisplayName() : "");
                // Search
                cmbSyncProvider.addValueChangeListener(event -> {
                    if (event.getValue() != null) {
                        wdwReleaseConfiguration.getBtnConfirm().setClassName("confirm-button");
                        wdwReleaseConfiguration.getBtnConfirm().setEnabled(true);
                    } else {
                        wdwReleaseConfiguration.getBtnConfirm().removeClassName("confirm-button");
                        wdwReleaseConfiguration.getBtnConfirm().setEnabled(false);
                    }
                });
                //add to dialog
                wdwReleaseConfiguration.setContent(cmbSyncProvider);
            } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                        ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), DeleteSyncDataSourceConfigurationAction.class));
            }
            wdwReleaseConfiguration.getBtnConfirm().addClickListener((event) -> {
                wdwReleaseConfiguration.close();
                try {
                    ActionResponse actionResponse
                            = runSynchronizationAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>(PARAM_SYNC_DATA_SOURCE, configuration),
                            new ModuleActionParameter<>(PARAM_SYNC_PROVIDER, cmbSyncProvider.getValue())));

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

            });
            return wdwReleaseConfiguration;
        } else {
            return new Dialog(new Label(ts.getTranslatedString("module.sync.actions.sync-release-sync-data-source-configuration.error")));
        }
    }

    @Override
    public AbstractAction getModuleAction() {
        return runSynchronizationAction;
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
