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
package org.neotropic.kuwaiba.modules.optional.contactman.actions;

import com.vaadin.flow.component.UI;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates a new contact
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewContactAction extends AbstractAction {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to Business Entity Manager
     */
    @Autowired
    private BusinessEntityManager bem;
    
    @PostConstruct
    protected void init() {
        this.id = "contactman.new-contact";
        this.displayName = ts.getTranslatedString("module.contactman.actions.new-contact.name");
        this.description = ts.getTranslatedString("module.contactman.actions.new-contact.description");
        this.order = 1000;
        
        setCallback((parameters) -> {
            Session session = UI.getCurrent().getSession().getAttribute(Session.class);
            String name = (String) parameters.get(Constants.PROPERTY_NAME);
            ClassMetadataLight contact = (ClassMetadataLight) parameters.get("contact");
            BusinessObjectLight customer = (BusinessObjectLight) parameters.get("customer");

            HashMap<String, String> property = new HashMap<>();
            property.put(Constants.PROPERTY_NAME, name);
            try {  
                String oid;
                oid = bem.createContact(contact.getName(), customer.getClassName(), customer.getId(),  session.getUser().getUserName());
                bem.updateObject(contact.getName(), oid, property);
            } catch (BusinessObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException 
                    | OperationNotPermittedException | ApplicationObjectNotFoundException ex) {
                throw new ModuleActionException(ex.getMessage());
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