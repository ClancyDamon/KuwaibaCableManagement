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

import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.sync.SynchronizationService;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncDataSourceConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Release Sync Data Source Configuration.
 *
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class ReleaseSyncDataSourceConfigurationAction extends AbstractAction {
    /**
     * Parameter, group to be released from datasource.
     */
    public static String PARAM_RELEASE_GROUPS = "groups"; //NOI18N
    /**
     * Parameter, data source configuration.
     */
    public static String PARAM_SYNC_DATA_SOURCE = "syncDataSourceConfiguration"; //NOI18N
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Business Entity Manager
     */
    @Autowired
    private SynchronizationService ss;

    @PostConstruct
    protected void init() {
        this.id = ts.getTranslatedString("sync.new-sync-release-sync-data-source-configuration");
        this.displayName = ts.getTranslatedString("module.sync.actions.sync-release-sync-data-source-configuration.name");
        this.description = ts.getTranslatedString("module.sync.actions.sync-release-sync-data-source-configuration.description");
        this.order = 1000;

        setCallback((parameters) -> {
            Long[] synchronizationGroups = (Long[]) parameters.get(PARAM_RELEASE_GROUPS);
            SyncDataSourceConfiguration configuration = (SyncDataSourceConfiguration) parameters.get(PARAM_SYNC_DATA_SOURCE);
            ActionResponse actionReesponse = new ActionResponse();
            try {
                ss.releaseSyncDataSourceConfigFromSyncGroups(synchronizationGroups, configuration.getId());
            } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
                actionReesponse.put("exception", ex);
            }
            return actionReesponse;
        });
    }

    @Override
    public int getRequiredAccessLevel() {
        return Privilege.ACCESS_LEVEL_READ_WRITE;
    }

    @Override
    public boolean requiresConfirmation() {
        return false;
    }
}