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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates a single business object.
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class NewBusinessObjectAction extends AbstractAction {
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
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    
    @PostConstruct
    protected void init() {
        this.id = "navigation.new-business-object";
        this.displayName = ts.getTranslatedString("module.navigation.actions.new-single-business-object.name");
        this.description = ts.getTranslatedString("module.navigation.actions.new-business-object.description");
        this.icon = new Icon(VaadinIcon.PLUS);
        this.order = 1;
        
        setCallback(parameters -> {
            try {
                Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                String className = (String) parameters.get(Constants.PROPERTY_CLASSNAME);
                String parentClassName = (String) parameters.get(Constants.PROPERTY_PARENT_CLASS_NAME);
                String parentOid = (String) parameters.get(Constants.PROPERTY_PARENT_ID);
                HashMap<String, String> attributes = (HashMap) parameters.get(Constants.PROPERTY_ATTRIBUTES);
                String uuid = bem.createObject(className, parentClassName, parentOid, attributes, null);
                // activity log
                aem.createObjectActivityLogEntry(session.getUser().getUserName(), className, uuid,
                        ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, Constants.PROPERTY_ID, "", uuid, "");
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
