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
package org.neotropic.kuwaiba.modules.commercial.sync;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.sync.actions.AssociateSyncDataSourceToGroupVisualAction;
import org.neotropic.kuwaiba.modules.commercial.sync.actions.DeleteSyncDataSourceConfigurationVisualAction;
import org.neotropic.kuwaiba.modules.commercial.sync.actions.DeleteSynchronizationGroupVisualAction;
import org.neotropic.kuwaiba.modules.commercial.sync.actions.DeleteTemplateDataSourceVisualAction;
import org.neotropic.kuwaiba.modules.commercial.sync.actions.NewSyncDataSourceConfigurationVisualAction;
import org.neotropic.kuwaiba.modules.commercial.sync.actions.NewSyncGroupVisualAction;
import org.neotropic.kuwaiba.modules.commercial.sync.actions.ReleaseSyncDataSourceConfigurationVisualAction;
import org.neotropic.kuwaiba.modules.commercial.sync.actions.RunSingleSynchronizationVisualAction;
import org.neotropic.kuwaiba.modules.commercial.sync.actions.RunSynchronizationVisualAction;
import org.neotropic.kuwaiba.modules.commercial.sync.components.JobProgressMessage;
import org.neotropic.kuwaiba.modules.commercial.sync.components.ProgressBroadcaster;
import org.neotropic.kuwaiba.modules.commercial.sync.components.ProgressViewDialog;
import org.neotropic.kuwaiba.modules.commercial.sync.components.SyncDataSourceTab;
import org.neotropic.kuwaiba.modules.commercial.sync.components.SyncGroupTab;
import org.neotropic.kuwaiba.modules.commercial.sync.components.TemplateDataSourceTab;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncResult;
import org.neotropic.kuwaiba.modules.commercial.sync.notification.Broadcaster;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Main for the Synchronization Module. This class manages how the pages
 * corresponding to different functionalities are presented in a single place.
 *
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@StyleSheet("css/sync.css")
@Route(value = "sync", layout = SynchronizationLayout.class)
public class SynchronizationUI extends VerticalLayout implements HasDynamicTitle,
        ActionCompletedListener, AbstractUI {


    /**
     * main the content
     */
    private VerticalLayout mainLayout;
    private Registration broadcasterRegistration;
    /**
     * Reference to the Translation Service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Business Entity Manager
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Application Entity Manager
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Synchronization Service
     */
    @Autowired
    private SynchronizationService ss;
    /**
     * Factory to build resources from data source.
     */
    @Autowired
    private ResourceFactory resourceFactory;
    /**
     * The visual action for add a new Sync Data Source Configuration to sync
     * group
     */
    @Autowired
    private AssociateSyncDataSourceToGroupVisualAction associateSyncDataSourceToGroupVisualAction;
    /**
     * The visual action for add a new Sync Data Source Configuration to sync
     * group
     */
    @Autowired
    private NewSyncDataSourceConfigurationVisualAction newSyncDataSourceConfigurationVisualAction;
    /**
     * The visual action for delete Sync Data group
     */
    @Autowired
    private DeleteSynchronizationGroupVisualAction deleteSynchronizationGroupVisualAction;
    /**
     * The visual action for delete Sync Data Source Configuration
     */
    @Autowired
    private DeleteSyncDataSourceConfigurationVisualAction deleteSyncDataSourceConfigurationVisualAction;
    /**
     * The visual action for delete Sync Data Source Configuration
     */
    @Autowired
    private DeleteTemplateDataSourceVisualAction deleteTemplateDataSourceVisualAction;
    /**
     * The visual action for release Sync Data Source Configuration
     */
    @Autowired
    private ReleaseSyncDataSourceConfigurationVisualAction releaseSyncDataSourceConfigurationVisualAction;
    /**
     * The visual action for release Sync Data Source Configuration
     */
    @Autowired
    private NewSyncGroupVisualAction newSyncGroupVisualAction;
    /**
     * The visual action for run group Synchronization data source
     */
    @Autowired
    private RunSingleSynchronizationVisualAction runSingleSynchronizationVisualAction;
    /**
     * The visual action for run group Synchronization group
     */
    @Autowired
    private RunSynchronizationVisualAction runSynchronizationVisualAction;
    /**
     * Vaadin thread broadcaster
     */
    @Autowired
    private Broadcaster broadcaster;
    /**
     * Vaadin thread broadcaster
     */
    @Autowired
    private ProgressBroadcaster progressBroadcaster;

    private ProgressViewDialog progressViewDialog;

    public SynchronizationUI() {
        super();
        setSizeFull();
        setMargin(false);
        setSpacing(false);
        setPadding(false);
    }

    @Override
    public void initContent() {

        mainLayout = new VerticalLayout();
        mainLayout.setMargin(false);
        mainLayout.setSpacing(false);
        mainLayout.setPadding(false);
        // Main Layout
        SyncGroupTab tabSyncGroup = new SyncGroupTab(ts.getTranslatedString("module.sync.sync-group.title"),
                true, true, deleteSynchronizationGroupVisualAction,
                releaseSyncDataSourceConfigurationVisualAction,
                newSyncGroupVisualAction,
                runSynchronizationVisualAction,
                bem, ss, ts);
        SyncDataSourceTab tabSyncDataSource = new SyncDataSourceTab(ts.getTranslatedString("module.sync.data-source.title"),
                true, true,
                associateSyncDataSourceToGroupVisualAction,
                deleteSyncDataSourceConfigurationVisualAction,
                releaseSyncDataSourceConfigurationVisualAction,
                runSingleSynchronizationVisualAction,
                resourceFactory,
                bem, aem, ss, ts);
        TemplateDataSourceTab tabTemplateSataSource = new TemplateDataSourceTab(ts.getTranslatedString("module.sync.template-data-source.title"),
                true, true,
                deleteTemplateDataSourceVisualAction,
                bem, aem, ss, ts);
        //set tab and selected tab 
        Tabs tabs = new Tabs(tabSyncGroup, tabSyncDataSource, tabTemplateSataSource);
        tabs.setWidthFull();
        tabs.setSelectedTab(tabSyncGroup);

        //set visible content tab
        tabSyncGroup.getTabContent().setVisible(true);
        //switching tabs
        tabs.addSelectedChangeListener(event -> {
            mainLayout.removeAll();
            if (event.getSelectedTab().equals(tabSyncDataSource)) {
                tabSyncGroup.getTabContent().setVisible(false);
                tabTemplateSataSource.getTabContent().setVisible(false);
                tabSyncDataSource.getTabContent().setVisible(true);
                mainLayout.add(tabSyncDataSource.getTabContent());
            } else if (event.getSelectedTab().equals(tabSyncGroup)) {
                tabSyncDataSource.getTabContent().setVisible(false);
                tabTemplateSataSource.getTabContent().setVisible(false);
                tabSyncGroup.getTabContent().setVisible(true);
                mainLayout.add(tabSyncGroup.getTabContent());
            } else if (event.getSelectedTab().equals(tabTemplateSataSource)) {
                tabSyncGroup.getTabContent().setVisible(false);
                tabSyncDataSource.getTabContent().setVisible(false);
                tabTemplateSataSource.getTabContent().setVisible(true);
                mainLayout.add(tabTemplateSataSource.getTabContent());
            }
        });
        //set default content
        mainLayout.add(tabSyncGroup.getTabContent());
        add(tabs, mainLayout);
    }

    private void progressView (JobProgressMessage progress, List<SyncResult> syncResults ){


        if(progressViewDialog  == null) {
            progressViewDialog = new ProgressViewDialog(ts);
            progressViewDialog.updateValues(progress, syncResults);
            progressViewDialog.open();
            progressViewDialog.getBtnConfirm().addClickListener( event -> {
                progressViewDialog.close();
                progressViewDialog = null;
            });
        } else {
            progressViewDialog.updateValues(progress, syncResults);
        }
    }

    @Override
    public void onAttach(AttachEvent attachEvent) {
        this.associateSyncDataSourceToGroupVisualAction.registerActionCompletedLister(this);
        this.deleteSynchronizationGroupVisualAction.registerActionCompletedLister(this);
        this.deleteSyncDataSourceConfigurationVisualAction.registerActionCompletedLister(this);
        this.deleteTemplateDataSourceVisualAction.registerActionCompletedLister(this);
        this.releaseSyncDataSourceConfigurationVisualAction.registerActionCompletedLister(this);
        this.newSyncDataSourceConfigurationVisualAction.registerActionCompletedLister(this);
        this.runSingleSynchronizationVisualAction.registerActionCompletedLister(this);
        this.newSyncGroupVisualAction.registerActionCompletedLister(this);
        this.runSynchronizationVisualAction.registerActionCompletedLister(this);
        UI ui = attachEvent.getUI();

        broadcasterRegistration = broadcaster.register(syncMessage ->
                ui.access(() ->
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), syncMessage,
                                AbstractNotification.NotificationType.INFO, ts).open()
                )
        );

        broadcasterRegistration = progressBroadcaster.registerSyncResults((progress, syncResults) ->
                ui.access(() -> progressView(progress, syncResults)));
    }

    @Override
    public void onDetach(DetachEvent ev) {
        this.associateSyncDataSourceToGroupVisualAction.unregisterListener(this);
        this.deleteSynchronizationGroupVisualAction.unregisterListener(this);
        this.deleteSyncDataSourceConfigurationVisualAction.unregisterListener(this);
        this.deleteTemplateDataSourceVisualAction.unregisterListener(this);
        this.releaseSyncDataSourceConfigurationVisualAction.unregisterListener(this);
        this.newSyncDataSourceConfigurationVisualAction.unregisterListener(this);
        this.runSingleSynchronizationVisualAction.unregisterListener(this);
        this.newSyncGroupVisualAction.unregisterListener(this);
        this.runSynchronizationVisualAction.unregisterListener(this);
        broadcasterRegistration.remove();
    }

    @Override
    public void actionCompleted(ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS)
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                    AbstractNotification.NotificationType.INFO, ts).open();
        else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.sync.title");
    }
}
