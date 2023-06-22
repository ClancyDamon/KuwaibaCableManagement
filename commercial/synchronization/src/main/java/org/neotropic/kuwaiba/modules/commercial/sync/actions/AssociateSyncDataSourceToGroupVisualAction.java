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
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.sync.SynchronizationModule;
import org.neotropic.kuwaiba.modules.commercial.sync.SynchronizationService;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncDataSourceConfiguration;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SynchronizationGroup;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Visual wrapper of configure data source.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@neotropic.co>}
 */
@Component
public class AssociateSyncDataSourceToGroupVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * New business object visual action parameter business object.
     */
    public static String PARAM_SYNC_DATA_SOURCE = "syncDataSourceConfiguration"; //NOI18N
    /**
     * Parameter group.
     */
    public static String PARAM_GROUP = "groups"; //NOI18N
    /**
     * Parameter exception.
     */
    public static String PARAM_EXCEPTION = "exception"; //NOI18N
    /**
     * Parameter command close.
     */
    public static String PARAM_COMMANDCLOSE = "commandClose"; //NOI18N
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
    private AssociateSyncDataSourceToGroupAction associateSyncDataSourceToGroupAction;
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

    public AssociateSyncDataSourceToGroupVisualAction() {
        super(SynchronizationModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        SyncDataSourceConfiguration selectedObject;
        if (parameters.containsKey(PARAM_SYNC_DATA_SOURCE)) {
            selectedObject = (SyncDataSourceConfiguration) parameters.get(PARAM_SYNC_DATA_SOURCE);
            commandClose = (Command) parameters.get(PARAM_COMMANDCLOSE);

            wdwConfigureDataSource = new ConfirmDialog(ts,
                    ts.getTranslatedString("module.sync.actions.get-sync-group.filter-header"),
                    ts.getTranslatedString("module.sync.actions.new-sync-data-source-configuration.add-to-sync-group")
            );
            wdwConfigureDataSource.getBtnConfirm().setEnabled(false);
            wdwConfigureDataSource.setResizable(true);
            wdwConfigureDataSource.setDraggable(true);
            //dialog body
            MultiSelectListBox<SynchronizationGroup> listGroups = new MultiSelectListBox<>();
            Icon iconSearch = VaadinIcon.SEARCH.create();
            TextField txtSearchClass = new TextField();
            try {
                List<SynchronizationGroup> groups = ss.getSyncGroups();
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
                    if (value != null && !value.isEmpty()) {
                        listGroups.setItems(groups.stream()
                                .filter(item -> item.toString().toLowerCase().contains(value.toLowerCase()))
                                .collect(Collectors.toList()));
                    } else {
                        listGroups.setItems(groups);
                    }
                });
                // Sync group list
                listGroups.setItems(groups);
                listGroups.setRenderer(new ComponentRenderer<>(item -> {
                    return new Label(item.getName());
                }));
                listGroups.addValueChangeListener(event -> {
                    wdwConfigureDataSource.getBtnConfirm().setEnabled(!listGroups.getSelectedItems().isEmpty());
                });
                // Add content to dialog
                wdwConfigureDataSource.setContent(txtSearchClass, listGroups);
            } catch (InvalidArgumentException | MetadataObjectNotFoundException | UnsupportedPropertyException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                        ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), AssociateSyncDataSourceToGroupAction.class));
            }
            // Configurate
            wdwConfigureDataSource.getBtnConfirm().addClickListener(event -> {
                Long[] arrayGroups = listGroups.getSelectedItems().stream().map(item -> item.getId()).toArray(size -> new Long[size]);
                try {
                    ActionResponse actionResponse
                            = associateSyncDataSourceToGroupAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter(PARAM_SYNC_DATA_SOURCE, selectedObject),
                            new ModuleActionParameter(PARAM_GROUP, arrayGroups)));

                    if (actionResponse.containsKey(PARAM_EXCEPTION)) {
                        success = false;
                        throw new ModuleActionException(((Exception) actionResponse.get(PARAM_EXCEPTION)).getLocalizedMessage());
                    }
                } catch (ModuleActionException ex) {
                    success = false;
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                            ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), AssociateSyncDataSourceToGroupAction.class));
                }
                wdwConfigureDataSource.close();

                if (success == true) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                            ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            String.format(ts.getTranslatedString("module.sync.actions.new-sync-data-source-configuration.add-to-sync-group.success"),
                                    selectedObject.getName()),
                            AssociateSyncDataSourceToGroupAction.class));

                }
            });
            return wdwConfigureDataSource;
        } else {
            return new Dialog(new Label(ts.getTranslatedString("module.general.messages.object-not-found")));
        }
    }

    @Override
    public AbstractAction getModuleAction() {
        return associateSyncDataSourceToGroupAction;
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