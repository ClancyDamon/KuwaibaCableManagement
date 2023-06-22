/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.core.configuration.filters.actions;

import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ScriptNotCompiledException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Delete a filter definition.
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class UpdateFilterDefinitionAction extends AbstractAction {   
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    
    @PostConstruct
    protected void init() {
        this.id = "configman.update-filter-definition";
        this.displayName = ts.getTranslatedString("module.configman.filters.actions.edit-filter");
        this.description = ts.getTranslatedString("module.configman.filters.actions.edit-filter.label.edit");
        this.order = 1000;

        setCallback((parameters) -> {
            long filterDefinitionId = (long) parameters.get(Constants.PROPERTY_ID);
            String name = (String) parameters.get(Constants.PROPERTY_NAME);
            String description_ = (String) parameters.get(Constants.PROPERTY_DESCRIPTION);
            String classToBeApplied = (String) parameters.get(Constants.PROPERTY_CLASSNAME);
            String script = (String) parameters.get(Constants.PROPERTY_SCRIPT);
            Boolean isEnable = (Boolean) parameters.get(Constants.PROPERTY_ENABLED);
            
            try {
                aem.updateFilterDefinition(filterDefinitionId, name, description_, classToBeApplied, script, isEnable);
            } catch (ApplicationObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException | ScriptNotCompiledException ex) {
                throw new ModuleActionException(ex.getLocalizedMessage());
            }
            return new ActionResponse();
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