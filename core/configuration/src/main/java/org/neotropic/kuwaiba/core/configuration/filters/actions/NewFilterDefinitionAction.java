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

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Create a new Filter definition.
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class NewFilterDefinitionAction extends AbstractAction {
    /**
     * Used to retrieve into the ui the last created filter and select this 
     * filter in the filter grid
     */
    public static final String PARAM_NEW_FILTER_DEFINITION = "newFilter";
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
        this.id = "configman.new-filter-definition";
        this.displayName = ts.getTranslatedString("module.configman.filters.actions.new-filter.name");
        this.description = ts.getTranslatedString("module.configman.filters.actions.new-filter.description");
        this.icon = new Icon(VaadinIcon.PLUS);
        this.icon.setSize("12px");
        this.order = 1000;

        setCallback((parameters) -> {
            String name = (String) parameters.get(Constants.PROPERTY_NAME);
            String description_ = (String) parameters.get(Constants.PROPERTY_DESCRIPTION);
            String classToBeApplied = (String) parameters.get(Constants.PROPERTY_CLASSNAME);
            String script = (String) parameters.get(Constants.PROPERTY_SCRIPT);
            boolean isEnable = (boolean) parameters.get(Constants.PROPERTY_ENABLED);
            
            try {
                aem.createFilterDefinition(name, description_, classToBeApplied, script, isEnable);
            } catch (InvalidArgumentException | MetadataObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
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