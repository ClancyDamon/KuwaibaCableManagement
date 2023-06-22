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
 * Creates a relation between sync data source configuration and a sync group.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@neotropic.co>}
 */
@Component
public class AssociateSyncDataSourceToGroupAction extends AbstractAction {
    /**
     * New business object visual action parameter business object.
     */
    public static String SYNC_DATA_SOURCE = "syncDataSourceConfiguration"; //NOI18N
    /**
     * Parameter group.
     */
    public static String PARAM_GROUP = "groups";
    /**
     * Reference to the Translation Service.s
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
        this.id = ts.getTranslatedString("sync.new-sync-data-source-configuration");
        this.displayName = ts.getTranslatedString("module.sync.actions.new-sync-data-source-configuration.name");
        this.description = ts.getTranslatedString("module.sync.actions.new-sync-data-source-configuration.description");
        this.order = 1000;

        setCallback((parameters) -> {
            SyncDataSourceConfiguration object = (SyncDataSourceConfiguration) parameters.get(SYNC_DATA_SOURCE);
            Long[] synchronizationGroups = (Long[]) parameters.get(PARAM_GROUP);

            ActionResponse actionReesponse = new ActionResponse();
            try {
                ss.associateSyncDataSourceToGroup(object.getId(), synchronizationGroups);
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