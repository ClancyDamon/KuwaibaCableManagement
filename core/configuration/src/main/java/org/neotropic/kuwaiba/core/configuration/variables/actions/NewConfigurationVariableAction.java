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
package org.neotropic.kuwaiba.core.configuration.variables.actions;

import com.vaadin.flow.component.UI;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates a new Configuration Variable.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewConfigurationVariableAction extends AbstractAction {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the application entity manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    
    @PostConstruct
    protected void init() {
        this.id = "configvarman.new-configuration-variable";
        this.displayName = ts.getTranslatedString("module.configman.actions.new-configuration-variable.name");
        this.description = ts.getTranslatedString("module.configman.actions.new-configuration-variable.description");
        this.order = 1000;
        
        setCallback((parameters) -> {
            Session session = UI.getCurrent().getSession().getAttribute(Session.class);
            String configVariablesPoolId = (String) parameters.get(Constants.PROPERTY_ID);
            String name = (String) parameters.get(Constants.PROPERTY_NAME);
            String descriptions = (String) parameters.get(Constants.PROPERTY_DESCRIPTION);
            String valueDefinition = (String) parameters.get(Constants.PROPERTY_VALUE);
            Boolean masked = (Boolean) parameters.get(Constants.PROPERTY_MASKED);
            Integer type = (Integer) parameters.get(Constants.PROPERTY_TYPE);
            ActionResponse actionResponse = new ActionResponse();
            try {
                aem.createConfigurationVariable(configVariablesPoolId
                        , name
                        , descriptions
                        , type
                        , masked
                        , valueDefinition
                );
                aem.createGeneralActivityLogEntry(session.getUser().getUserName(), ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT,
                    String.format(ts.getTranslatedString("module.configman.actions.new-configuration-variable.ui.created-log"), name));
            } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
                actionResponse.put("exception", ex);
            }
            return actionResponse;
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