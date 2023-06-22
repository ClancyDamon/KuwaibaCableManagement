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
import org.neotropic.kuwaiba.modules.commercial.sync.model.SynchronizationGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Create a new sync data source configuration.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@neotropic.co>}
 */
@Component
public class NewSyncGroupAction extends AbstractAction {

    /**
     * Parameter group.
     */
    public static String PARAM_GROUP = "group";
    /**
     * Parameter group.
     */
    public static String PARAM_EXCEPTION = "exception";
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
        this.id = ts.getTranslatedString("sync.new-sync-data-source-configuration");
        this.displayName = ts.getTranslatedString("module.sync.actions.new-sync-data-source-configuration.name");
        this.description = ts.getTranslatedString("module.sync.actions.new-sync-data-source-configuration.description");
        this.order = 1000;
        ActionResponse actionReesponse = new ActionResponse();
        setCallback((parameters) -> {
            SynchronizationGroup group = (SynchronizationGroup) parameters.get(PARAM_GROUP);
            if (group != null) {
                try {
                    saveSyncGroup(group);
                } catch (ApplicationObjectNotFoundException ex) {
                    actionReesponse.put(PARAM_EXCEPTION, ex);
                }
            } else
                actionReesponse.put(PARAM_EXCEPTION, "Synchronization group not found");

            return actionReesponse;
        });
    }


    /**
     * save data of synchronization Group
     *
     * @param synchronizationGroup synchronization Group
     */
    private void saveSyncGroup(SynchronizationGroup group) throws ApplicationObjectNotFoundException {
        if (group.getName() != null && group.getDescription() != null) {
            try {
                ss.createSyncGroup(group);
            } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
                throw new ApplicationObjectNotFoundException(String.format("%s. %s"
                        , ts.getTranslatedString("module.general.messages.information")
                        , ex.getMessage()));
            }
        } else {
            throw new ApplicationObjectNotFoundException(String.format("%s. %s"
                    , ts.getTranslatedString("module.general.messages.information")
                    , ts.getTranslatedString("error.module.sync.new.data-source.properties")));
        }
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