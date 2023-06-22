/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.modules.core.navigation.actions;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates a single business object as a special object (as in the special containment hierarchy).
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class NewSpecialBusinessObjectAction extends AbstractAction {
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    
    @PostConstruct
    protected void init() {
        this.id = "navigation.new-special-business-object";
        this.displayName = ts.getTranslatedString("module.navigation.actions.new-special-single-business-object.name");
        this.description = ts.getTranslatedString("module.navigation.actions.new-special-business-object.description");
        this.icon = new Icon(VaadinIcon.PLUS_CIRCLE);
        this.order = 2;
        
        setCallback(parameters -> {
            try {
                String className = (String) parameters.get(Constants.PROPERTY_CLASSNAME);
                String parentClassName = (String) parameters.get(Constants.PROPERTY_PARENT_CLASS_NAME);
                String parentOid = (String) parameters.get(Constants.PROPERTY_PARENT_ID);
                HashMap<String, String> attributes = (HashMap) parameters.get(Constants.PROPERTY_ATTRIBUTES);
                bem.createSpecialObject(className, parentClassName, parentOid, attributes, null);
                return new ActionResponse();
            } catch (InventoryException ex) {
                throw new ModuleActionException(ex.getMessage());
            }
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
